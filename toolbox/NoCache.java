/*    */ package com.android.volley.toolbox;
/*    */ 
/*    */ import com.android.volley.Cache;
/*    */ import com.android.volley.Cache.Entry;
/*    */ 
/*    */ public class NoCache
/*    */   implements Cache
/*    */ {
/*    */   public void clear()
/*    */   {
/*    */   }
/*    */ 
/*    */   public Cache.Entry get(String key)
/*    */   {
/* 31 */     return null;
/*    */   }
/*    */ 
/*    */   public void put(String key, Cache.Entry entry)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void invalidate(String key, boolean fullExpire)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void remove(String key)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void initialize()
/*    */   {
/*    */   }
/*    */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.NoCache
 * JD-Core Version:    0.5.4
 */