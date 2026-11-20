"""Render current PDFs/SVGs for visual review without external tools."""
import pathlib
import fitz
from PIL import Image, ImageDraw

R=pathlib.Path(__file__).resolve().parents[3]
O=R/'results/step-1'
images=[]
for level in ['physical','logical','conceptual']:
    folder=O/(level+'-schema')
    doc=fitz.open(folder/(level+'.pdf'))
    for i,p in enumerate(doc):
        pix=p.get_pixmap()
        images.append((level+' PDF '+str(i+1),Image.frombytes('RGB',[pix.width,pix.height],pix.samples)))
    svg=fitz.open(folder/(level+'.svg'))
    page=svg[0]
    scale=600/page.rect.width
    pix=page.get_pixmap(matrix=fitz.Matrix(scale,scale),alpha=False)
    pix.save(O/'metrics'/(level+'-svg-preview.png'))
canvas=Image.new('RGB',(1600,((len(images)+3)//4)*320),'#dce3ea')
draw=ImageDraw.Draw(canvas)
for i,(name,im) in enumerate(images):
    im.thumbnail((390,285));x=(i%4)*400;y=(i//4)*320
    canvas.paste(im,(x,y+23));draw.text((x+5,y+4),name,fill='black')
canvas.save(O/'metrics/pdf-contact-sheet.png')
print('Rendered current PDF contact sheet:',len(images),'pages; SVG previews: 3')
