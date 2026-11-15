"""Generate the submission report from recorded results and actual screenshots.
Requires reportlab, Pillow and DejaVu fonts. Reads no credentials.
"""
from pathlib import Path
from html import escape
import re
from io import BytesIO
from PIL import Image
from reportlab.lib.utils import ImageReader
from reportlab.pdfgen import canvas
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import Paragraph
from reportlab.lib.styles import ParagraphStyle

def screenshot_image(path):
    # Preserve original PNG evidence; compress only the PDF's embedded copy.
    image = Image.open(path).convert('RGB')
    image.thumbnail((720, 1280), Image.Resampling.LANCZOS)
    data = BytesIO()
    image.save(data, format='JPEG', quality=88, optimize=True)
    data.seek(0)
    return ImageReader(data)

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/project-report.pdf'
for name, filename in [('Body','DejaVuSans.ttf'),('Bold','DejaVuSans-Bold.ttf')]:
    pdfmetrics.registerFont(TTFont(name, '/usr/share/fonts/truetype/dejavu/' + filename))
W,H=A4
c=canvas.Canvas(str(OUT),pagesize=A4)
c.setTitle('Travel Destination Explorer - Project Report')
c.setAuthor('Ravitejas; Dharoor Shiva Sai Varun; Gowtham B')
style=ParagraphStyle('body',fontName='Body',fontSize=10,leading=15,textColor=colors.HexColor('#25304b'))
page=0

def begin(title):
    global page
    page+=1
    c.setFillColor(colors.HexColor('#1c2d50'));c.rect(0,H-100,W,100,fill=1,stroke=0)
    c.setFillColor(colors.white);c.setFont('Bold',19);c.drawString(42,H-55,title)
    c.setFont('Body',9);c.drawString(42,H-78,'MAADK mini project | 16 November 2026')
    c.setFillColor(colors.HexColor('#647089'));c.setFont('Body',8)
    c.drawString(42,24,'Travel Destination Explorer | Emulator evidence; physical demonstration pending')
    c.drawRightString(W-42,24,str(page))

def para(text,y,x=42,width=W-84):
    p=Paragraph(text,style);_,h=p.wrap(width,700)
    if y-h<46:raise ValueError('Page overflow: '+text[:60])
    p.drawOn(c,x,y-h);return y-h-12

def heading(text,y):
    c.setFillColor(colors.HexColor('#1c2d50'));c.setFont('Bold',13);c.drawString(42,y,text);return y-23

begin('Travel Destination Explorer')
y=H-128
y=heading('Country discovery and local trip planning',y)
y=para('An Android application that combines live country information and geographic exploration with a persistent wishlist and dated trips. Users can discover a destination, inspect its facts and photo, save it and create a trip through one navigation flow.',y)
y=heading('Team and contributions',y)
for name,role in [('Ravitejas','Explore/Home, combined search and filters, shared navigation, mandatory feature integration, regression checks and submission artifacts.'),('Dharoor Shiva Sai Varun','API/details, states and cities, tourist attractions and interactive maps; geography reliability, coordinate mapping and attraction filtering fixes.'),('Gowtham B','Room database, Wishlist and Trip Planner foundation; trip CRUD/date verification and database singleton correction.')]:
    y=para('<b>'+name+'</b><br/>'+role,y)
y=heading('Delivered scope',y)
y=para('Country search and region filters; facts/flags and twelve featured destination photos; wishlist save/remove; trip create/read/edit/delete with dates and notes; preferred-region persistence; browser research and sharing; states, cities, nearby attractions and maps.',y)
y=para('PR #10 is merged into main at 223f875. The final release adds submission evidence and a correction to a city-card test; it does not add new planner destinations or redesign screens. Original teammate contribution history is retained.',y)
c.showPage()

begin('Architecture and requirement coverage')
y=H-128
y=para('<b>Architecture:</b> Jetpack Compose/Material 3 screens use ViewModels and repositories. Retrofit/Gson supplies country, PlaceDB and Overpass data; Glide loads remote photos. Room stores Wishlist and Trips. SharedPreferences stores the preferred Explore region. OpenStreetMap displays attraction markers.',y)
for name,detail in [('Compose and navigation','Explore, Details, Wishlist, Trips, trip form, states, cities, attractions and map are connected in one app.'),('Retrofit and Glide','API-backed country/geography data and remote destination photos with attribution and failure placeholders.'),('Room','Wishlist and trip entities/DAOs; duplicate-save protection; awaited writes; create, edit, delete and reopen persistence.'),('SharedPreferences','Preferred Explore region is restored after app restart. Search text remains temporary.'),('Android resources','App string resources and a visible app-specific empty-state vector.'),('Implicit intents','ACTION_VIEW opens destination research in a browser. ACTION_SEND opens a chooser for destination/trip text; errors are handled.'),('Correctness','Country pagination, cancellation, Alpha-2/Alpha-3 identity, coordinate validation, matching map radius/query, strict dates and confirmed deletion.'),('Submission and viva','Repository source, setup instructions, screenshots, recording and this report. Physical-device demonstration and individual viva remain team responsibilities.')]:
    y=para('<b>'+name+'</b><br/>'+detail,y)
c.showPage()

begin('Verification and practical limits')
y=H-128
results=(ROOT/'docs/submission-verification-summary.txt').read_text().strip()
for line in results.splitlines():y=para(escape(line),y)
y=heading('What the evidence means',y)
y=para('The recording contains actual emulator interaction with idle waits trimmed. Screenshot captions identify the displayed behavior. A share chooser is opened and cancelled; no message is sent. An emulator pass is not a physical-phone pass.',y)
c.showPage()
begin('Limits and final demonstration')
y=H-128
y=heading('Known limits',y)
y=para('Destination photos cover twelve featured countries rather than every country. Online country browsing, photos, PlaceDB, Overpass and map tiles require connectivity; saved text remains local. Public services can be unavailable or slow. Glide caching does not guarantee offline photo coverage.',y)
y=para('Trip destinations currently select a country. Optional finer selection is estimated separately: 4-8 person-hours for states/cities, or 8-16 hours total including attraction selection, a non-destructive Room migration and regression checks. It is not part of this release.',y)
y=heading('Remaining human demonstration',y)
y=para('Ravitejas will run the latest submitted revision on a USB-debuggable physical phone. Record device model, Android version and revision, then complete docs/phone-demonstration.md. Each member prepares their contribution explanation and one complete cross-module flow for the individual viva.',y)
c.showPage()

captions={
'01-explore':'Explore: live country list and shared navigation',
'02-search-region':'Case-insensitive search combined with region filtering',
'03-country-details':'Country details: destination photo and planning actions',
'04-wishlist':'Wishlist: saved destination and trip prefill',
'05-trip-create':'Trip form: title, country, dates and notes',
'06-trip-list':'Saved trip with edit, share and delete actions',
'07-trip-edit':'Editing the saved trip',
'08-trip-after-restart':'Edited trip remains after force-stop/relaunch',
'09-trip-share':'Android share chooser; cancelled without sending',
'10-delete-confirmation':'Confirmation before deleting a disposable trip',
'11-empty-trips':'Trips empty state after confirmed deletion',
'12-country-facts':'Country facts and geographic navigation',
'13-states':'Country states/provinces and local search',
'14-cities':'Cities in Karnataka; Bengaluru search',
'15-attractions':'Nearby tourist attraction results',
'16-attractions-radius':'5 km results restored after a remote 3 km failure',
'17-map':'OpenStreetMap markers for Bengaluru (5 km)',
'18-map-detail':'Selected map marker and attraction details',
'19-browser':'Research destination opens an external browser',
'20-preference-restored':'Preferred region restored after relaunch',
'21-offline-saved':'Saved Wishlist remains accessible offline',
'22-no-results':'Country search empty state',
'23-date-validation':'Reversed trip dates are rejected',
'24-country-share':'Destination sharing through the Android chooser',
'25-empty-wishlist':'Wishlist empty state after removal',
}
shots=[]
for path in sorted((ROOT/'docs/screenshots/submission').glob('*.png')):
    if path.stem in captions:shots.append((path,captions[path.stem]))
if not shots:raise ValueError('No submission screenshots')
for i in range(0,len(shots),2):
    begin('Application screens')
    for j,(path,caption) in enumerate(shots[i:i+2]):
        x=42+j*260
        para(escape(caption),H-126,x,245)
        c.drawImage(screenshot_image(path),x,115,width=245,height=490,preserveAspectRatio=True,anchor='c')
    c.showPage()

begin('Photo sources and licenses')
y=H-128
y=para('Remote photos are cropped to fit. Each source provides the original file, authorship and full license terms. In-app attribution links and docs/photo-credits.md retain the same credits.',y)
source=(ROOT/'docs/photo-credits.md').read_text()
for block in source.split('## ')[1:]:
    title=block.splitlines()[0]
    author=re.search(r'- Author: (.*)',block).group(1)
    license=re.search(r'- License: \[(.*?)\]',block).group(1)
    url=re.search(r'\[Original source and attribution\]\((.*?)\)',block).group(1)
    y=para(f'<b>{escape(title.replace(chr(8212), '-').replace(chr(8211), '-'))}</b><br/>{escape(author)} - {escape(license)} - <link href="{escape(url,quote=True)}" color="#375d9c">Original source and license</link>',y)
c.showPage();c.save();print(OUT)
