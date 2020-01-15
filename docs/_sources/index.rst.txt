.. SimSearch App documentation master file, created by
   sphinx-quickstart on Sun Jan  5 00:12:17 2020.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

SimSearch APP
=============


.. image:: images/figure1.png
    :width: 900px
    :align: right

**SimSearch** is a pattern-search algorithm that provides biologists 
with the ability, once they identify an interesting genomic pattern, to
look for similar occurrences in the data, thus facilitating genomic
data access and use.

For example, such patterns can describe **gene
expression regulatory DNA areas** including heterogeneous (epi)genomic
features (e.g. **histone modification** and/or different **transcription
factor binding regions**).


It is possible to define complex patterns
based on **perfect matching** in genome tracks (regions that must
match), **partial matching** (regions that are allowed to be absent),
and **negative matchings** (for instance to search for regions distant
from transcription start sites).

Index
-----

.. toctree::
   :maxdepth: 2

   install
   usage
   example


Video
-----

Have a look at `https://www.youtube.com/watch?v=jW65ope1h1o <https://www.youtube.com/watch?v=jW65ope1h1o>`_
to see the SimSearch plugin in action:

.. raw:: html

    <div><iframe width="560" height="315" src="https://www.youtube.com/embed/jW65ope1h1o" frameborder="0" allowfullscreen></iframe></div>



Acknowlegments:
---------------

The SimSearch algorithm has been developed as a collaboration between
`University of Bologna <https://www.unibo.it/en">`_ and the `Politecnico di Milano <https://www.polimi.it/en/>`_.

The **SimSearch App** for `IGB <http://bioviz.org/igb/>`_, based on the **SimSearch algorithm*, has been developped by `Arnaud Ceol <https://github.com/arnaudceol>`_ for the `Istituto Italiano di Tecnologia <http://iit.it/centers/cgs-semm>`_.

**SimSearch** is part of the `DATA-DRIVEN GENOMIC COMPUTING(GeCo) <http://www.bioinformatics.deib.polimi.it/geco/>`_ project.

.. image:: http://www.bioinformatics.deib.polimi.it/geco/imgs/logo-geco.png
    :width: 150px



Support
----------

- Issue Tracker: `<https://github.com/DEIB-GECO/simsearch-app/issues>`_
- Source Code: `<ttps://github.com/DEIB-GECO/simsearch-app>`_


License
-------

The SimSearch IGB App and its documentation are freely available for 
non-commercial use.
