/*     */ package com.android.volley;
/*     */ 
/*     */ import android.os.Handler;
/*     */ import java.util.concurrent.Executor;
/*     */ 
/*     */ public class ExecutorDelivery
/*     */   implements ResponseDelivery
/*     */ {
/*     */   private final Executor mResponsePoster;
/*     */ 
/*     */   public ExecutorDelivery(Handler handler)
/*     */   {
/*  36 */     this.mResponsePoster = new Executor(handler)
/*     */     {
/*     */       public void execute(Runnable command) {
/*  39 */         this.val$handler.post(command);
/*     */       }
/*     */     };
/*     */   }
/*     */ 
/*     */   public ExecutorDelivery(Executor executor)
/*     */   {
/*  50 */     this.mResponsePoster = executor;
/*     */   }
/*     */ 
/*     */   public void postResponse(Request<?> request, Response<?> response)
/*     */   {
/*  55 */     postResponse(request, response, null);
/*     */   }
/*     */ 
/*     */   public void postResponse(Request<?> request, Response<?> response, Runnable runnable)
/*     */   {
/*  60 */     request.markDelivered();
/*  61 */     request.addMarker("post-response");
/*  62 */     this.mResponsePoster.execute(new ResponseDeliveryRunnable(request, response, runnable));
/*     */   }
/*     */ 
/*     */   public void postError(Request<?> request, VolleyError error)
/*     */   {
/*  67 */     request.addMarker("post-error");
/*  68 */     Response response = Response.error(error);
/*  69 */     this.mResponsePoster.execute(new ResponseDeliveryRunnable(request, response, null));
/*     */   }
/*     */ 
/*     */   private class ResponseDeliveryRunnable
/*     */     implements Runnable
/*     */   {
/*     */     private final Request mRequest;
/*     */     private final Response mResponse;
/*     */     private final Runnable mRunnable;
/*     */ 
/*     */     public ResponseDeliveryRunnable(Request request, Response response, Runnable runnable)
/*     */     {
/*  83 */       this.mRequest = request;
/*  84 */       this.mResponse = response;
/*  85 */       this.mRunnable = runnable;
/*     */     }
/*     */ 
/*     */     public void run()
/*     */     {
/*  92 */       if (this.mRequest.isCanceled()) {
/*  93 */         this.mRequest.finish("canceled-at-delivery");
/*  94 */         return;
/*     */       }
/*     */ 
/*  98 */       if (this.mResponse.isSuccess())
/*  99 */         this.mRequest.deliverResponse(this.mResponse.result);
/*     */       else {
/* 101 */         this.mRequest.deliverError(this.mResponse.error);
/*     */       }
/*     */ 
/* 106 */       if (this.mResponse.intermediate)
/* 107 */         this.mRequest.addMarker("intermediate-response");
/*     */       else {
/* 109 */         this.mRequest.finish("done");
/*     */       }
/*     */ 
/* 113 */       if (this.mRunnable != null)
/* 114 */         this.mRunnable.run();
/*     */     }
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.ExecutorDelivery
 * JD-Core Version:    0.5.4
 */