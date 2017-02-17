package com.android.volley.toolbox;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import java.io.IOException;
import java.util.Map;
import org.apache.http.HttpResponse;

public abstract interface HttpStack
{
  public abstract HttpResponse performRequest(Request<?> paramRequest, Map<String, String> paramMap)
    throws IOException, AuthFailureError;
}

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.HttpStack
 * JD-Core Version:    0.5.4
 */