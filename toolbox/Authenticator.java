package com.android.volley.toolbox;

import com.android.volley.AuthFailureError;

public abstract interface Authenticator
{
  public abstract String getAuthToken()
    throws AuthFailureError;

  public abstract void invalidateAuthToken(String paramString);
}

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.Authenticator
 * JD-Core Version:    0.5.4
 */