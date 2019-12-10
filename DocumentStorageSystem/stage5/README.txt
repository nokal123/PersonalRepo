{\rtf1\ansi\ansicpg1252\cocoartf1671
{\fonttbl\f0\fnil\fcharset0 HelveticaNeue;}
{\colortbl;\red255\green255\blue255;\red0\green0\blue0;}
{\*\expandedcolortbl;;\cssrgb\c0\c0\c0;}
{\*\listtable{\list\listtemplateid1\listhybrid{\listlevel\levelnfc0\levelnfcn0\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelspace360\levelindent0{\*\levelmarker \{decimal\}.}{\leveltext\leveltemplateid1\'02\'00.;}{\levelnumbers\'01;}\fi-360\li720\lin720 }{\listname ;}\listid1}}
{\*\listoverridetable{\listoverride\listid1\listoverridecount0\ls1}}
\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\deftab720
\pard\pardeftab720\sl280\partightenfactor0

\f0\fs24 \cf2 \expnd0\expndtw0\kerning0
assumptions for CS\
\
\pard\tx220\tx720\pardeftab720\li720\fi-720\sl280\partightenfactor0
\ls1\ilvl0\cf2 \kerning1\expnd0\expndtw0 {\listtext	1.	}\expnd0\expndtw0\kerning0
protected methods and private fields are allowed for any class\
\ls1\ilvl0\kerning1\expnd0\expndtw0 {\listtext	2.	}\expnd0\expndtw0\kerning0
as long as a class extends or implements the proper interface of abstract class, it does not need to be generic\
\ls1\ilvl0\kerning1\expnd0\expndtw0 {\listtext	3.	}\expnd0\expndtw0\kerning0
that in a case where one does a put that replaces a document, you do not move the old document to disk, but delete it completely. Therefor undoing such an action and putting the old doc back in would not create new lamdas, since the old lamda when it was first put in still exists\'a0\
\ls1\ilvl0\kerning1\expnd0\expndtw0 {\listtext	4.	}\expnd0\expndtw0\kerning0
when one does a get on a document and pulls it back into memory from the disk, it does memory management to see what needs to be removed. in such a case each document that gets removed because of said memory management has its own undo/redo lamdas created and added to the stack. at the end, the original document that was moved from disk to memory gets its own lamda\
\ls1\ilvl0\kerning1\expnd0\expndtw0 {\listtext	5.	}\expnd0\expndtw0\kerning0
any object can have as many protected methods as it likes\
\ls1\ilvl0\kerning1\expnd0\expndtw0 {\listtext	6.	}\expnd0\expndtw0\kerning0
I can have an instance of documentIO in DSI\
}