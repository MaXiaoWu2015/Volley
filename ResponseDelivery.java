package com.android.volley;

public abstract interface ResponseDelivery
{
  public abstract void postResponse(Request<?> paramRequest, Response<?> paramResponse);

  public abstract void postResponse(Request<?> paramRequest, Response<?> paramResponse, Runnable paramRunnable);

  public abstract void postError(Request<?> paramRequest, VolleyError paramVolleyError);
}

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.ResponseDelivery
 * JD-Core Version:    0.5.4
 */