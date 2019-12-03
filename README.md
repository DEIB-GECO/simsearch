
**SimSearch** is an **IGB** http://bioviz.org/igb/  plugin to identify and compare occurrences of (epi)genomics feature patterns in multiple genome browser tracks. 

It is based on a **pattern-search algorithm** that provides biologists   with   the   ability,   once   they   identify   an   interesting genomic  pattern,  to  look  for  similar  occurrences  in  the  data,  thus
facilitating   genomic   data   access   and   use.   For   example,   such patterns   can   describe   gene   expression   regulatory   DNA   areas including   heterogeneous   (epi)genomic   features   (e.g.   **histone modification** and/or different **transcription factor binding regions**). It is possible to define complex patterns based on perfect matches in  genome  tracks  (regions  that  must  match),  partial  matches (regions that are allowed to be absent), and negative matches (for instance  to  search  for  regions  distant  from  transcription  start sites).

The SimSearch algorithm has been developed through a collaboration between University of Bologna and Politecnico di Milano, and is included in the SimSearch App as part of the [**DATA-DRIVEN GENOMIC COMPUTING
(GeCo)**] http://www.bioinformatics.deib.polimi.it/geco/ project.

The SimSearch App for IGB is developped by @arnaudceol for the Istituto Italiano di Tecnologia.

For more information, documentation and example, look at the [**SimSearch project URL**] https://deib-geco.github.io/simsearch/, or this [WIKI] https://github.com/DEIB-GECO/simsearch/wiki  for a quick installation guide. 


** Quickstart: **

To compile the plugin and create a local repository:

`mvn initialize clean bundle-markdown-encoder:encodeMarkdown install bundle:index`

