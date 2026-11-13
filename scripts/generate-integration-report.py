"""Generate the review PDF from verified integration screenshots and public credits.
Run with a Python environment containing reportlab. No credentials are read.
"""
from pathlib import Path
from html import escape
import re
from reportlab.pdfgen import canvas
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import Paragraph
from reportlab.lib.styles import ParagraphStyle

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/integration-review.pdf'
pdfmetrics.registerFont(TTFont('DejaVu','/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'))
pdfmetrics.registerFont(TTFont('DejaVu-Bold','/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf'))
style = ParagraphStyle('body',fontName='DejaVu',fontSize=10,leading=15,textColor=colors.HexColor('#25304b'))
c = canvas.Canvas(str(OUT),pagesize=A4)
c.setTitle('Travel Destination Explorer - Integration Review')
W,H=A4
page=0
def begin(title):
 global page
 page+=1
 c.setFillColor(colors.HexColor('#1c2d50'));c.rect(0,H-100,W,100,fill=1,stroke=0)
 c.setFillColor(colors.white);c.setFont('DejaVu-Bold',19);c.drawString(42,H-55,title)
 c.setFont('DejaVu',9);c.drawString(42,H-78,'MAADK mini project | Integration review | 14 November 2026')
 c.setFillColor(colors.HexColor('#647089'));c.setFont('DejaVu',8)
 c.drawString(42,24,'Emulator evidence - physical-device and final submission checks remain open')
 c.drawRightString(W-42,24,str(page))
def para(text,y,x=42,width=None):
 p=Paragraph(text,style);_,height=p.wrap(width or W-84,700);p.drawOn(c,x,y-height);return y-height-12
def heading(text,y):
 c.setFillColor(colors.HexColor('#1c2d50'));c.setFont('DejaVu-Bold',13);c.drawString(42,y,text);return y-23
begin('Travel Destination Explorer')
y=H-128
y=heading('Connected discovery and local trip planning',y)
y=para('An Android app built with Kotlin, Jetpack Compose/Material 3, Retrofit/Gson, Glide, Room and SharedPreferences. The integrated experience connects country discovery to saved destinations and dated trips, while retaining the existing geographic exploration and map module.',y)
y=heading('Team and contributions',y)
for name,role in [('Ravitejas','Explore/Home, shared navigation, integration and regression checks.'),('Dharoor Shiva Sai Varun','API/details, states/cities, attractions and interactive map foundation.'),('Gowtham B','Room database, Wishlist and Trips foundation.')]:y=para(f'<b>{name}</b> - {role}',y)
y=heading('Implementation in this integration',y)
y=para('Country search plus region filters; 12 curated destination photos with attribution; save/remove wishlist; create/read/edit/delete trips; destination prefill; date pickers and strict validation; persisted Explore region; browser research and sharing; complete country pagination and safer geographic navigation.',y)
y=heading('Review status',y)
y=para('PR #2 and PR #3 are combined with original author history preserved. The integration is prepared on a separate codex branch for team review. Existing PRs have not been automatically merged or closed. Student identifiers and final institutional formatting should be added by the team before submitting a final report.',y)
y=para('<b>Limitations:</b> this is a review report, not a claim of completed physical-device verification. Photos cover twelve featured countries. APIs, photos and map tiles require connectivity. Saved country/trip text remains local. Optional features are deferred.',y)
c.showPage()
begin('Verification and requirement coverage')
y=H-128
y=para('<b>Verified command:</b> assembleDebug, testDebugUnitTest, lintDebug and connectedDebugAndroidTest completed successfully on the integrated source. Retained logs/XML are outside Git in Travel-Explorer-Integration-Evidence.',y)
y=para('<b>Results:</b> 15 unit tests and 12 emulator tests passed, zero failures or skips. Lint: zero errors, 55 warnings and one hint. Emulator: Travel_Phase1_API37, Android 17/API 37.1. No physical Android device was connected.',y)
for title,detail in [('Compose / navigation (8)','One Explore implementation, stateful navigation, wishlist and trip forms. Core live journey and saved-state tests pass.'),('Retrofit (5) and Glide (required)','Live Explore/details test passes. Pagination/failure regressions pass. Twelve photo URLs responded successfully and were visually reviewed.'),('Room (5)','Database close/reopen test covers saved wishlist, duplicate prevention and trip CRUD. Awaited saves, repeated taps and failed-save retry are tested.'),('SharedPreferences (2)','Preferred region is wired into Explore. Preference recreation/reset regression passes.'),('Resources (2)','String resources and visible app-specific empty-state vector used.'),('Two implicit intents (2)','ACTION_SEND chooser and ACTION_VIEW browser helper implemented with unavailable-handler protection. Actual target-app verification remains a device checklist item.'),('Best practices (2)','ViewModel/repository separation, stable country identity, strict dates, cancellation, error handling and focused tests.'),('Device demo (4) and viva (10)','Physical-device run and individual viva remain team responsibilities; do not substitute emulator claims.')]:
 y=para(f'<b>{title}</b><br/>{detail}',y)
c.showPage()
begin('Ownership and release checklist')
y=H-128
for title,text in [('Ravitejas - issues #4 and #7','Reviewable integration PR, navigation/preferences/intents/photos, evidence and artifact assembly. Estimates in issues are person-hour ranges, not availability commitments.'),('Gowtham - issue #5','Verify Room and trip behavior against the integrated build: force-stop/relaunch, duplicate saves, CRUD, strict dates, cancellation and errors. Avoid duplicating implementation.'),('Varun - issue #6','Review country paging/errors, actual Alpha-2 values, valid coordinates, radius consistency and existing states/cities/attractions/map flow.'),('All - issue #8','Physical-device verification, final complete-functionality video, screenshots/PDF, README and individual viva. Target submission Monday 16 November; viva Tuesday 17 November 2026.')]:y=para(f'<b>{title}</b><br/>{text}',y)
y=heading('Final acceptance before submission',y)
y=para('Run the submitted revision on a physical Android device and record its model/Android version/commit. Show full wishlist/trip CRUD, restart persistence, both external actions, photo errors/offline saved screens and verified geographic extras. Upload a final video and PDF with all final screens. Check repository artifact links and keep credentials out of source, recordings and reports.',y)
y=heading('Individual viva preparation',y)
y=para('Explain Compose state and recomposition, ViewModel work, SharedPreferences versus Room, entities/DAOs and stable IDs, Retrofit/Gson pagination and cancellation, Glide/photo attribution, implicit intents and resources. Each member explains their own contribution and one complete cross-module flow. Distinguish fixtures, live emulator results and physical-device results.',y)
c.showPage()
shots=[('01-explore.png','Explore: live country list and shared navigation'),('02-photo-search.png','Search results with real destination photography'),('03-details-photo.png','Details: photo credits, save and planning actions'),('04-wishlist.png','Wishlist: saved destination and Plan trip'),('05-trip-form.png','Trip form: destination prefill and selected dates'),('06-trips.png','Trips: persisted dates, edit/delete/share actions')]
valid=[(name,caption) for name,caption in shots if (ROOT/'docs/screenshots/integration'/name).exists() and (ROOT/'docs/screenshots/integration'/name).stat().st_size]
for i in range(0,len(valid),2):
 begin('Integrated app screenshots')
 for j,(name,caption) in enumerate(valid[i:i+2]):
  x=42+j*260
  para(escape(caption),H-126,x,245)
  c.drawImage(str(ROOT/'docs/screenshots/integration'/name),x,H-620,width=245,height=435,preserveAspectRatio=True,anchor='c')
 c.showPage()
begin('Destination photo credits')
y=H-128
y=para('Images are fetched remotely and cropped to fit. The following source links contain original files, authorship and full license terms. This is a curated catalogue rather than global photo coverage.',y)
source=(ROOT/'docs/photo-credits.md').read_text()
for block in source.split('## ')[1:]:
 title=block.splitlines()[0];author=re.search(r'- Author: (.*)',block).group(1);license=re.search(r'- License: \[(.*?)\]',block).group(1);url=re.search(r'\[Original source and attribution\]\((.*?)\)',block).group(1)
 y=para(f'<b>{escape(title)}</b><br/>{escape(author)} - {escape(license)} - <link href="{escape(url,quote=True)}" color="#375d9c">Original source and license</link>',y)
c.showPage();c.save()
print(OUT)
