package org.onosproject.usdn.protocol;

public enum  USDNversion {
    USDN(1);

    public final double  usdnversion;

    private USDNversion(double usdnversion)
    {
        this.usdnversion = usdnversion;
    }

    public double getUSDNversion()
    {
        return this.usdnversion;
    }
}
