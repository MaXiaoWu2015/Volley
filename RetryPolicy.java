package com.android.volley;

public abstract interface RetryPolicy
{
  public abstract int getCurrentTimeout();

  public abstract int getCurrentRetryCount();

  public abstract void retry(VolleyError paramVolleyError)
    throws VolleyError;
}

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.RetryPolicy
 * JD-Core Version:    0.5.4
 */