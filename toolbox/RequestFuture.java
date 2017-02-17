/*     */ package com.android.volley.toolbox;
/*     */ 
/*     */ import com.android.volley.Request;
/*     */ import com.android.volley.Response.ErrorListener;
/*     */ import com.android.volley.Response.Listener;
/*     */ import com.android.volley.VolleyError;
/*     */ import java.util.concurrent.ExecutionException;
/*     */ import java.util.concurrent.Future;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import java.util.concurrent.TimeoutException;
/*     */ 
/*     */ public class RequestFuture<T>
/*     */   implements Future<T>, Response.Listener<T>, Response.ErrorListener
/*     */ {
/*     */   private Request<?> mRequest;
/*  57 */   private boolean mResultReceived = false;
/*     */   private T mResult;
/*     */   private VolleyError mException;
/*     */ 
/*     */   public static <E> RequestFuture<E> newFuture()
/*     */   {
/*  62 */     return new RequestFuture();
/*     */   }
/*     */ 
/*     */   public void setRequest(Request<?> request)
/*     */   {
/*  68 */     this.mRequest = request;
/*     */   }
/*     */ 
/*     */   public synchronized boolean cancel(boolean mayInterruptIfRunning)
/*     */   {
/*  73 */     if (this.mRequest == null) {
/*  74 */       return false;
/*     */     }
/*     */ 
/*  77 */     if (!isDone()) {
/*  78 */       this.mRequest.cancel();
/*  79 */       return true;
/*     */     }
/*  81 */     return false;
/*     */   }
/*     */ 
/*     */   public T get() throws InterruptedException, ExecutionException
/*     */   {
/*     */     try
/*     */     {
/*  88 */       return doGet(null);
/*     */     } catch (TimeoutException e) {
/*  90 */       throw new AssertionError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public T get(long timeout, TimeUnit unit)
/*     */     throws InterruptedException, ExecutionException, TimeoutException
/*     */   {
/*  97 */     return doGet(Long.valueOf(TimeUnit.MILLISECONDS.convert(timeout, unit)));
/*     */   }
/*     */ 
/*     */   private synchronized T doGet(Long timeoutMs) throws InterruptedException, ExecutionException, TimeoutException
/*     */   {
/* 102 */     if (this.mException != null) {
/* 103 */       throw new ExecutionException(this.mException);
/*     */     }
/*     */ 
/* 106 */     if (this.mResultReceived) {
/* 107 */       return this.mResult;
/*     */     }
/*     */ 
/* 110 */     if (timeoutMs == null)
/* 111 */       super.wait(0L);
/* 112 */     else if (timeoutMs.longValue() > 0L) {
/* 113 */       super.wait(timeoutMs.longValue());
/*     */     }
/*     */ 
/* 116 */     if (this.mException != null) {
/* 117 */       throw new ExecutionException(this.mException);
/*     */     }
/*     */ 
/* 120 */     if (!this.mResultReceived) {
/* 121 */       throw new TimeoutException();
/*     */     }
/*     */ 
/* 124 */     return this.mResult;
/*     */   }
/*     */ 
/*     */   public boolean isCancelled()
/*     */   {
/* 129 */     if (this.mRequest == null) {
/* 130 */       return false;
/*     */     }
/* 132 */     return this.mRequest.isCanceled();
/*     */   }
/*     */ 
/*     */   public synchronized boolean isDone()
/*     */   {
/* 137 */     return (this.mResultReceived) || (this.mException != null) || (isCancelled());
/*     */   }
/*     */ 
/*     */   public synchronized void onResponse(T response)
/*     */   {
/* 142 */     this.mResultReceived = true;
/* 143 */     this.mResult = response;
/* 144 */     super.notifyAll();
/*     */   }
/*     */ 
/*     */   public synchronized void onErrorResponse(VolleyError error)
/*     */   {
/* 149 */     this.mException = error;
/* 150 */     super.notifyAll();
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.RequestFuture
 * JD-Core Version:    0.5.4
 */