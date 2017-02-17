/*     */ package com.android.volley.toolbox;
/*     */ 
/*     */ import android.accounts.Account;
/*     */ import android.accounts.AccountManager;
/*     */ import android.accounts.AccountManagerFuture;
/*     */ import android.content.Context;
/*     */ import android.content.Intent;
/*     */ import android.os.Bundle;
/*     */ import com.android.volley.AuthFailureError;
/*     */ 
/*     */ public class AndroidAuthenticator
/*     */   implements Authenticator
/*     */ {
/*     */   private final AccountManager mAccountManager;
/*     */   private final Account mAccount;
/*     */   private final String mAuthTokenType;
/*     */   private final boolean mNotifyAuthFailure;
/*     */ 
/*     */   public AndroidAuthenticator(Context context, Account account, String authTokenType)
/*     */   {
/*  45 */     this(context, account, authTokenType, false);
/*     */   }
/*     */ 
/*     */   public AndroidAuthenticator(Context context, Account account, String authTokenType, boolean notifyAuthFailure)
/*     */   {
/*  57 */     this(AccountManager.get(context), account, authTokenType, notifyAuthFailure);
/*     */   }
/*     */ 
/*     */   AndroidAuthenticator(AccountManager accountManager, Account account, String authTokenType, boolean notifyAuthFailure)
/*     */   {
/*  63 */     this.mAccountManager = accountManager;
/*  64 */     this.mAccount = account;
/*  65 */     this.mAuthTokenType = authTokenType;
/*  66 */     this.mNotifyAuthFailure = notifyAuthFailure;
/*     */   }
/*     */ 
/*     */   public Account getAccount()
/*     */   {
/*  73 */     return this.mAccount;
/*     */   }
/*     */ 
/*     */   public String getAuthToken() throws AuthFailureError
/*     */   {
/*  80 */     AccountManagerFuture future = this.mAccountManager.getAuthToken(this.mAccount, 
/*  81 */       this.mAuthTokenType, this.mNotifyAuthFailure, null, null);
/*     */     Bundle result;
/*     */     try
/*     */     {
/*  84 */       result = (Bundle)future.getResult();
/*     */     } catch (Exception e) {
/*  86 */       throw new AuthFailureError("Error while retrieving auth token", e);
/*     */     }
/*     */     Bundle result;
/*  88 */     String authToken = null;
/*  89 */     if ((future.isDone()) && (!future.isCancelled())) {
/*  90 */       if (result.containsKey("intent")) {
/*  91 */         Intent intent = (Intent)result.getParcelable("intent");
/*  92 */         throw new AuthFailureError(intent);
/*     */       }
/*  94 */       authToken = result.getString("authtoken");
/*     */     }
/*  96 */     if (authToken == null) {
/*  97 */       throw new AuthFailureError("Got null auth token for type: " + this.mAuthTokenType);
/*     */     }
/*     */ 
/* 100 */     return authToken;
/*     */   }
/*     */ 
/*     */   public void invalidateAuthToken(String authToken)
/*     */   {
/* 105 */     this.mAccountManager.invalidateAuthToken(this.mAccount.type, authToken);
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.AndroidAuthenticator
 * JD-Core Version:    0.5.4
 */