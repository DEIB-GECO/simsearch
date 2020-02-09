package it.iit.genomics.cru.simsearch.bundle.model;

import it.unibo.disi.simsearch.core.model.Parameters;

public class SimSearchParameters extends Parameters {

    protected int maxPeakLength = 0;

    @Override
    public int getMaxPeakLength() {
        return this.maxPeakLength;
    }

    public void setMaxPeakLength(int maxPeakLength) {
        this.maxPeakLength = maxPeakLength;
    }

    

}

