BDSup2Sub
=========

A tool to convert and tweak bitmap based subtitle streams
---------------------------------------------------------

BDSup2Sub is a tool initially created to convert captions demuxed from a Blu-Ray transport stream (M2TS) into the DVD VobSub format (SUB/IDX) used by many DVD authoring tools - hence the name. Many more features were added over time as was support for other formats. So in the meantime the name seems a little inappropriate. In a nutshell, it's a subtitle conversion tool for image based stream formats with scaling capabilities and some other nice features.

Supported Formats
-----------------

* Blu-Ray SUP: import (since 1.0) and export (since 1.6)
* Sony BDN XML (as used by Sonic Scenarist HDMV): import and export (since 3.3.0)
* HD-DVD SUP: import (since 1.9)
* VobSub (SUB/IDX): import  (since 3.5.0) and export (since 1.0)
* DVD-SUP (SUP/IFO): import and export (since 3.9.0)

What it can do
--------------

* convert any supported import format to any supported export format
* add a fixed delay to all timestamps (since 1.0)
* perform a frame rate conversion e.g. for pal speedup (since 1.0)
* synchronize time stamps to output frame rate (since 2.0)
* edit times and position of each caption (since 2.3)
* control all features (except editing) from the command line (since 1.7)
* move all captions inside or outside a given area (since 2.7) or horizontally (since 3.9.6)
* crop the target screen size (since 3.0)
* scale up/down with a variety of filters (from bilinear over bicubic to Lanczos3 and Mitchell) (since 3.4.0)
* edit "forced" flags (since 3.6.0)
* exclude single captions from export (since 3.6.0)
* erase rectangular regions of a caption (since 3.6.0)
* scale captions independently of screen size (since 3.7.0)
* automatically remove fade in/out from imported subtitle streams.
* export the target palette in PGCEdit text format (RGB values 0..255) (since 3.9.0)
* edit the imported DVD palette when input is either VobSub or SUP/IFO (since 3.9.1)
* edit the frame palette and alpha values when input is either VobSub or SUP/IFO (since 3.9.3)
* set/clear forced flags for all captions (since 3.9.9)

What it can't do
----------------

BDSup2Sub only supports image based subtitle streams. It doesn't (and probably won't ever) support  text based formats as SRT or SSA. Note that to convert image based subtitles to text based subtitles would need an OCR (Optical Character Recognition) approach which is beyond the idea of converting between image based formats. There are other tools like SubRip and SupRip to convert image based subtitles to SRT. While importing HD-DVD SUPs is supported, BDSup2Sub can't export this format currently - and with the commercial death of HD-DVD I think adding this feature would be a waste of time. 

Author
------

Originally created by 0xdeadbeef (2009); currently maintained by Miklos Juhasz (2011-)

Forum
-----

[Official thread on Doom9.org](http://forum.doom9.org/showthread.php?t=145277)

Feedback
--------

You can post comments and bug reports to the above thread. Feedback is welcome - especially from the authors of SubRip, SupRip, SubtitleCreator or TsMuxer to discuss compatibility issues. To report problems with a subtitle stream, please either post the stream (if it is compliant with the forum rules) or upload it to a "one click hoster" and send me a PM with the link. And don't forget a detailed error description. Make sure you contact the current maintainer mjuhasz and not the original author 0xdeadbeef.