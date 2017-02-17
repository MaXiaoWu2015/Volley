package com.android.volley;

public abstract interface Network
{
  public abstract NetworkResponse performRequest(Request<?> paramRequest)
    throws VolleyError;
}

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.Network
 * JD-Core Version:    0.5.4
 */