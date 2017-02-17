/*     */ package com.android.volley;
/*     */ 
/*     */ public class DefaultRetryPolicy
/*     */   implements RetryPolicy
/*     */ {
/*     */   private int mCurrentTimeoutMs;
/*     */   private int mCurrentRetryCount;
/*     */   private final int mMaxNumRetries;
/*     */   private final float mBackoffMultiplier;
/*     */   public static final int DEFAULT_TIMEOUT_MS = 2500;
/*     */   public static final int DEFAULT_MAX_RETRIES = 1;
/*     */   public static final float DEFAULT_BACKOFF_MULT = 1.0F;
/*     */ 
/*     */   public DefaultRetryPolicy()
/*     */   {
/*  48 */     this(2500, 1, 1.0F);
/*     */   }
/*     */ 
/*     */   public DefaultRetryPolicy(int initialTimeoutMs, int maxNumRetries, float backoffMultiplier)
/*     */   {
/*  58 */     this.mCurrentTimeoutMs = initialTimeoutMs;
/*  59 */     this.mMaxNumRetries = maxNumRetries;
/*  60 */     this.mBackoffMultiplier = backoffMultiplier;
/*     */   }
/*     */ 
/*     */   public int getCurrentTimeout()
/*     */   {
/*  68 */     return this.mCurrentTimeoutMs;
/*     */   }
/*     */ 
/*     */   public int getCurrentRetryCount()
/*     */   {
/*  76 */     return this.mCurrentRetryCount;
/*     */   }
/*     */ 
/*     */   public float getBackoffMultiplier()
/*     */   {
/*  83 */     return this.mBackoffMultiplier;
/*     */   }
/*     */ 
/*     */   public void retry(VolleyError error)
/*     */     throws VolleyError
/*     */   {
/*  92 */     this.mCurrentRetryCount += 1;
/*  93 */     this.mCurrentTimeoutMs = (int)(this.mCurrentTimeoutMs + this.mCurrentTimeoutMs * this.mBackoffMultiplier);
/*  94 */     if (!hasAttemptRemaining())
/*  95 */       throw error;
/*     */   }
/*     */ 
/*     */   protected boolean hasAttemptRemaining()
/*     */   {
/* 103 */     return this.mCurrentRetryCount <= this.mMaxNumRetries;
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.DefaultRetryPolicy
 * JD-Core Version:    0.5.4
 */