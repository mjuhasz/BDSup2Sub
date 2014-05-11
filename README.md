BDSup2Sub
=========

A tool to convert and tweak bitmap based subtitle streams
---------------------------------------------------------

BDSup2Sub is a tool initially created to convert captions demuxed from a Blu-Ray transport stream (M2TS) into the DVD VobSub format (SUB/IDX) used by many DVD authoring tools - hence the name. Many more features were added over time as was support for other formats. So in the meantime the name seems a little inappropriate. In a nutshell, it's a subtitle conversion tool for image based stream formats with scaling capabilities and some other nice features.

Supported Formats
-----------------

* Blu-Ray SUP
* Sony BDN XML (as used by Sonic Scenarist HDMV)
* HD-DVD SUP (import only)
* VobSub (SUB/IDX)
* DVD-SUP (SUP/IFO)

What it can do
--------------

* convert any supported import format to any supported export format
* add a fixed delay to all timestamps
* perform a frame rate conversion e.g. for pal speedup
* synchronize time stamps to output frame rate
* edit times and position of each caption
* control all features (except editing) from the command line
* move all captions inside or outside a given area or horizontally
* crop the target screen size
* scale up/down with a variety of filters (from bilinear over bicubic to Lanczos3 and Mitchell)
* edit "forced" flags
* exclude single captions from export
* erase rectangular regions of a caption
* scale captions independently of screen size
* automatically remove fade in/out from imported subtitle streams.
* export the target palette in PGCEdit text format (RGB values 0..255)
* edit the imported DVD palette when input is either VobSub or SUP/IFO
* edit the frame palette and alpha values when input is either VobSub or SUP/IFO
* set/clear forced flags for all captions

What it can't do
----------------

BDSup2Sub only supports image based subtitle streams. It doesn't (and probably won't ever) support text based formats as SRT or SSA. Note that to convert image based subtitles to text based subtitles would need an OCR (Optical Character Recognition) approach which is beyond the idea of converting between image based formats. There are other tools like SubRip and SupRip to convert image based subtitles to SRT. While importing HD-DVD SUPs is supported, BDSup2Sub can't export this format currently - and with the commercial death of HD-DVD I think adding this feature would be a waste of time. 

Author
------

Originally created by Volker Oth (2009); currently maintained and developed by Miklos Juhasz

More information
----------------

Visit the [Wiki pages](https://github.com/mjuhasz/BDSup2Sub/wiki)

License
-------

As of version 4.0.0, BDSup2Sub became an Open Source project. All files that are Volker Oth own creation are released under the Apache License 2.0. 
In a nutshell that means you can use them and modify them even for closed source or commercial projects, as long as you leave some copyright info in and give appropriate credit. See the linked Apache License for details.
Note that there are also some sources included which are based on the work of other people and thus have partly different license models:

- PNG Encoder by J. David Eisenberg (modified to support transparency for palette mode via the tRNS tag). Licensed under the LGPL.
- Octree Quantize Filter by Jerry Huxtable (modified to support alpha component and better palette reduction). Licensed under the Apache License 2.0.
- Scaling/filter routine based on Morten Nobel-Joergensen's ResampleOp from his Java image scaling library. Licensed under the LGPL.
- JFileFilter based on ExampleFileFilter from the FileChooserDemo included in the JDK. Licensed under proprietary Sun license (similar to Apache).

Each file of the source code should contain a header with the license information.
