import numpy as np
import cv2
from tkinter import *
from tkinter import filedialog
from tkinter.messagebox import showerror
from PIL import ImageTk,Image
import glob,os
import json
from tkinter import messagebox as tkMessageBox




def read_rgb_file(filename):
    filedata = open(filename,'rb')
    r=np.frombuffer(filedata.read(rows*cols),dtype=np.uint8).reshape(rows,cols)
    g=np.frombuffer(filedata.read(rows*cols),dtype=np.uint8).reshape(rows,cols)
    b=np.frombuffer(filedata.read(rows*cols),dtype=np.uint8).reshape(rows,cols)
    x=np.dstack((r,g,b))

    #imm = cv2.cvtColor(np.array(im,dtype=np.uint8),cv2.COLOR_BGR2RGB)
    #imm = ImageTk.PhotoImage(Image.fromarray(imm))
    return x




class AuthoringTool:
    def __init__(self, master):

        def refresh_left(val):
            if (self.filelist_1):
                self.left_frame_number = int(val)-1
                self.leftshow_frame = self.filelist_1[self.left_frame_number]
                self.imageleft = read_rgb_file(self.leftshow_frame)
                self.imageleft = ImageTk.PhotoImage(Image.fromarray(self.imageleft))
                self.left.create_image(0,0,image = self.imageleft, anchor = NW)
        def refresh_right(val):
            if (self.filelist_2):
                self.right_frame_number = int(val)-1
                self.rightshow_frame = self.filelist_2[self.right_frame_number]
                self.imageright = read_rgb_file(self.rightshow_frame)
                self.imageright = ImageTk.PhotoImage(Image.fromarray(self.imageright))
                self.right.create_image(0,0,image = self.imageright, anchor = NW)

        self.filelist_1 = []
        self.filelist_2 = []

        self.filedir1 = None
        self.filedir2 = None

        self.imageleft = None
        self.left_frame_number = None
        self.right_frame_number = None

        self.rect = None
        self.start_x = None
        self.start_y = None
        self.end_x = None
        self.end_y = None


        self.firstPressed = False
        self.secondPressed = False
        self.created = False
        self.connected = False
        self.typed = False

        self.track_info = {}
        self.master = master
        master.title("Hypermedia Authoring Tool")

        m1 = PanedWindow(master,orient=VERTICAL)
        m1.pack(expand=1)

        ###Display Two videos
        m2 = PanedWindow(m1)
        m1.add(m2)
        self.left = Canvas(m2,width=352,height=288)
        self.left.pack()
        m2.add(self.left)
        self.right = Canvas(m2,width=352,height=288)
        self.right.pack()
        m2.add(self.right)

        ###Display Sliders
        m3 = PanedWindow(m1)
        m1.add(m3)
        self.leftSlider = Scale(m3, variable = IntVar(), orient = HORIZONTAL,
                                sliderlength=10,length=352, from_=1, to=9000, command = refresh_left,state = DISABLED)
        m3.add(self.leftSlider)
        self.rightSlider = Scale(m3, variable = IntVar(), orient = HORIZONTAL,
                                sliderlength=10,length=352, from_=1, to=9000, command = refresh_right,state = DISABLED)
        m3.add(self.rightSlider)

        ####Display buttons
        m4 = PanedWindow(m1)
        m1.add(m4)
        #### Import buttons
        m5 = PanedWindow(m4,orient=VERTICAL)
        m4.add(m5)
        import1 = Button(m5, text="Import first video",highlightbackground='#3E4149', command=self.select_image_left)
        import2 = Button(m5, text="Import second video",highlightbackground='#3E4149', command=self.select_image_right)
        m5.add(import1)
        m5.add(import2)
        #### Create links button
        m6 = PanedWindow(m4,orient=VERTICAL)
        m4.add(m6)
        createlink = Button(m6, text="Create New Hyperlink",highlightbackground='#3E4149',command=self.create_link)
        connectvideo = Button(m6, text="Connect Videos",highlightbackground='#3E4149', command = self.connect_video)
        m6.add(createlink)
        m6.add(connectvideo)
        #### Link info
        m7 = PanedWindow(m4,orient=VERTICAL)
        m4.add(m7)
        m9 = PanedWindow(m7,orient = HORIZONTAL)
        m10 = PanedWindow(m7,orient = HORIZONTAL)
        m7.add(m9)
        m7.add(m10)
        scrollbar = Scrollbar(m9)
        scrollbar.pack(side = RIGHT)
        self.mylist = Listbox(m9,yscrollcommand = scrollbar.set, width=30, height =3 )
        self.mylist.pack(side = LEFT)
        scrollbar.config(command = self.mylist.yview)
        self.texttip = Label(m10,text = "Link Name")
        m10.add(self.texttip)
        self.newlinkname = StringVar()
        self.textcontent = Entry(m10, textvariable = self.newlinkname, state="disabled")
        m10.add(self.textcontent)
        m9.add(self.mylist)
        m9.add(scrollbar)

        #### Save file buttons
        m8 = PanedWindow(m4,orient=VERTICAL)
        m4.add(m8)
        savefile = Button(m8,text="Save File",highlightbackground='#3E4149', command = self.save_file)
        m8.add(savefile)



    def select_image_left(self):
        self.filelist_1 = []
        self.track_info = {}
        filedirectory = filedialog.askdirectory()
        #print (filedirectory)
        if filedirectory:
            self.mylist.delete(0,'end')
            os.chdir(filedirectory)
            self.filedir1 = filedirectory
            for fileab in glob.glob("*.rgb"):
                self.filelist_1 += [os.path.abspath(fileab)]
            self.filelist_1.sort()
            fname = self.filelist_1[0]
            if fname:
                try:
                    self.left_frame_number = 0
                    self.leftshow_frame = self.filelist_1[self.left_frame_number]
                    self.imageleft = read_rgb_file(self.leftshow_frame)
                    self.imageleft = ImageTk.PhotoImage(Image.fromarray(self.imageleft))
                    self.left.create_image(0,0,image = self.imageleft, anchor = NW)
                    self.firstPressed = True
                    self.leftSlider.config(state = NORMAL)
                except:
                    showerror("Open Source File", "Failed to read file\n'%s'" % fname)
        
                return
        
    def select_image_right(self):
        self.filelist_2 = []
        filedirectory = filedialog.askdirectory()
        if filedirectory:
            os.chdir(filedirectory)
            self.filedir2 = filedirectory
            for fileab in glob.glob("*.rgb"):
                self.filelist_2 += [os.path.abspath(fileab)]
            self.filelist_2.sort()
            fname = self.filelist_2[0]
            if fname:
                try:
                    self.right_frame_number = 0
                    self.rightshow_frame = self.filelist_2[self.right_frame_number]
                    self.imageright = read_rgb_file(self.rightshow_frame)
                    self.imageright = ImageTk.PhotoImage(Image.fromarray(self.imageright))
                    self.right.create_image(0,0,image = self.imageright, anchor = NW)
                    self.secondPressed = True
                    self.rightSlider.config(state = NORMAL)
                except:
                    showerror("Open Source File", "Failed to read file\n'%s'" % fname)
                return
    def create_link(self):
        def on_button_press(event):
            self.left.delete(self.rect)
            self.start_x = event.x
            self.start_y = event.y
            self.rect = self.left.create_rectangle(self.start_x,self.start_y,self.start_x,self.start_y, width = 3)
        def on_move_press(event):
            self.end_x,self.end_y = (event.x, event.y)
            self.left.coords(self.rect, self.start_x, self.start_y, self.end_x, self.end_y)
        def on_button_release(event):
            self.created = True
            pass
        def on_enter_press(event):
            typed_name = self.newlinkname.get()
            self.mylist.insert(END,typed_name)
            self.typed = True
        def on_delete_link(event):
            selectline = self.mylist.curselection()
            print(selectline[0])
            self.mylist.delete(selectline[0])
        # TODO
        if (self.firstPressed and self.secondPressed):
            self.textcontent.config(state="normal")
            self.textcontent.bind("<Return>",on_enter_press)
            self.mylist.bind("<BackSpace>",on_delete_link)
            self.left.bind("<ButtonPress-1>",on_button_press)
            self.left.bind("<B1-Motion>",on_move_press)
            self.left.bind("<ButtonRelease-1>",on_button_release)


    def connect_video(self):
        if self.created and self.typed:
            frameChosed = self.left_frame_number
            frameGoto = self.filelist_2[self.right_frame_number]
            self.bbox = (self.start_x, self.start_y, (self.end_x-self.start_x), (self.end_y-self.start_y))
            tracker1 = cv2.TrackerKCF_create()  
            tracker2 = cv2.TrackerKCF_create()
            frame = read_rgb_file(self.filelist_1[frameChosed])
            f1 = frameChosed
            f2 = frameChosed
            ok1 = tracker1.init(frame, self.bbox)
            ok2 = tracker2.init(frame, self.bbox)

            while ok1 and f1 >= 0:
                frame = read_rgb_file(self.filelist_1[f1])
                ok1, bbox = tracker1.update(frame)
                #p1 = (int(bbox[0]), int(bbox[1]))
                #p2 = (int(bbox[0] + bbox[2]), int(bbox[1] + bbox[3]))
                #cv2.rectangle(frame, p1, p2, (255,0,0), 2, 1)
                if f1 not in self.track_info:
                    self.track_info[f1] = [[str(int(bbox[0])),str(int(bbox[1])),str(int(bbox[2])),str(int(bbox[3])),frameGoto]]
                else:
                    self.track_info[f1].append([str(int(bbox[0])),str(int(bbox[1])),str(int(bbox[2])),str(int(bbox[3])),frameGoto])
                f1 -= 1
                #cv2.imshow("Trackingbackward", frame)
                #k = cv2.waitKey(1) & 0xff
                #if k == 27 : break
                #print (self.track_info)
            #cv2.destroyAllWindows()
            while ok2 and f2 <= 8999 :
                frame = read_rgb_file(self.filelist_1[f2])
                ok2, bbox = tracker2.update(frame)
                #p1 = (int(bbox[0]), int(bbox[1]))
                #p2 = (int(bbox[0] + bbox[2]), int(bbox[1] + bbox[3]))
                #cv2.rectangle(frame, p1, p2, (255,0,0), 2, 1)
                if f2 not in self.track_info:
                    self.track_info[f2] = [[str(int(bbox[0])),str(int(bbox[1])),str(int(bbox[2])),str(int(bbox[3])),frameGoto]]
                else:
                    self.track_info[f2].append([str(int(bbox[0])),str(int(bbox[1])),str(int(bbox[2])),str(int(bbox[3])),frameGoto])
                f2 += 1
                #cv2.imshow("Trackingforward", frame)
                #k = cv2.waitKey(1) & 0xff
                #if k == 27 : break
                #print (self.track_info)
            #cv2.destroyAllWindows()
            self.connected = True
            self.typed = False
            self.textcontent.config(state = "disabled")
            return

    def save_file(self):
        if self.connected:
            #TODO:
            j = json.dumps(self.track_info)
            fileObject = open(self.filedir1 + '.json', 'w')
            fileObject.write(j)
            fileObject.close()
            tkMessageBox.showinfo( " ","JSON file of "+ self.filedir1+" saved!")
            #print ("JSON file saved!")
            return



if __name__ == '__main__' :
    global rows
    global cols
    filelist_1 = None
    filelist_2 = None
    rows = 288
    cols = 352
    root = Tk()
    my_gui = AuthoringTool(root)
    root.mainloop()


#TODO: Test if it works when importing another video
#TODO: Check how it works when making multiple rectagles and how to forbid make rect after clicked connect videos

# ttk.Style().configure('blue/white.TButton', foreground='black', background='black')
# # panelA.place(x=10,y=10,width=288,height=352)
# self.panelA.pack(side="left", padx=10, pady=10)
# self.import_primary_button = ttk.Button(self.master, text="Import primary video", style = 'blue/white.TButton', command=self.select_image)
# self.import_primary_button.pack(side='bottom', fill=X, expand="yes",padx=10,pady=10)
