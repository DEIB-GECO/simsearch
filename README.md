# SimSearch: an IGB plugin to identify and compare occurrences of (epi)genomics feature patterns in multiple genome browser tracks. 

SimSearch is a pattern-search algorithm that provides biologists   with   the   ability,   once   they   identify   an   interesting genomic  pattern,  to  look  for  similar  occurrences  in  the  data,  thus
facilitating   genomic   data   access   and   use.   For   example,   such patterns   can   describe   gene   expression   regulatory   DNA   areas including   heterogeneous   (epi)genomic   features   (e.g.   histone modification and/or different transcription factor binding regions). It is possible to define complex patterns based on perfect matches in  genome  tracks  (regions  that  must  match),  partial  matches (regions that are allowed to be absent), and negative matches (for instance  to  search  for  regions  distant  from  transcription  start sites).

The SimSearch algorithm has been developed for the GenData project : http://gendata.weebly.com/, through a collaboration between University of Bologna and Politecnico di Milano. The SimSearch App for IGB is developped by @arnaudceol for the Istituto Italiano di Tecnologia.

Project URL: http://www-db.disi.unibo.it/research/GenData/SimSearch 


## Installation

### Install the Integrated Genome Browser (IGB)
The SimSearch App is a plugin for the Integrated Genome Browser (IGB) that can be downloaded from http://bioviz.org/igb/;

### Add the GenData repository:
From IGB, go to the “**plug-ins**” tab and launch the app manager. Add the GenData repository: press the “**add**” button, and insert “**GenData**” for name, and the url: **http://cru.genomics.iit.it/igb/plugins-simsearch/**. 

Then select the SimSearch plugin and press “**install**”. A new tab will appear in the tab panel in IGB.

### Add the SimSearch Quickload server
In order to make it easy to test the SimSearch app, we have compiled a Quickload repository with public data (mostly from ENCODE and Roadmap projects) for the human hg19 genome. To access this repository go to "**data access**", "**configure**", "**add...**", and add the repository:
name: "**SimSearch**"
url: "**http://cru.genomics.iit.it/simsearch-quickload/**"

Remember to select species "**Homo sapiens**", version "**H_sapiens_Feb_2009**" in order to see the datasets.




