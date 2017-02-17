/*     */ package com.android.volley.toolbox;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.Comparator;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ 
/*     */ public class ByteArrayPool
/*     */ {
/*  56 */   private List<byte[]> mBuffersByLastUse = new LinkedList();
/*  57 */   private List<byte[]> mBuffersBySize = new ArrayList(64);
/*     */ 
/*  60 */   private int mCurrentSize = 0;
/*     */   private final int mSizeLimit;
/*  69 */   protected static final Comparator<byte[]> BUF_COMPARATOR = new Comparator()
/*     */   {
/*     */     public int compare(byte[] lhs, byte[] rhs) {
/*  72 */       return lhs.length - rhs.length;
/*     */     }
/*  69 */   };
/*     */ 
/*     */   public ByteArrayPool(int sizeLimit)
/*     */   {
/*  80 */     this.mSizeLimit = sizeLimit;
/*     */   }
/*     */ 
/*     */   public synchronized byte[] getBuf(int len)
/*     */   {
/*  92 */     for (int i = 0; i < this.mBuffersBySize.size(); ++i) {
/*  93 */       byte[] buf = (byte[])this.mBuffersBySize.get(i);
/*  94 */       if (buf.length >= len) {
/*  95 */         this.mCurrentSize -= buf.length;
/*  96 */         this.mBuffersBySize.remove(i);
/*  97 */         this.mBuffersByLastUse.remove(buf);
/*  98 */         return buf;
/*     */       }
/*     */     }
/* 101 */     return new byte[len];
/*     */   }
/*     */ 
/*     */   public synchronized void returnBuf(byte[] buf)
/*     */   {
/* 111 */     if ((buf == null) || (buf.length > this.mSizeLimit)) {
/* 112 */       return;
/*     */     }
/* 114 */     this.mBuffersByLastUse.add(buf);
/* 115 */     int pos = Collections.binarySearch(this.mBuffersBySize, buf, BUF_COMPARATOR);
/* 116 */     if (pos < 0) {
/* 117 */       pos = -pos - 1;
/*     */     }
/* 119 */     this.mBuffersBySize.add(pos, buf);
/* 120 */     this.mCurrentSize += buf.length;
/* 121 */     trim();
/*     */   }
/*     */ 
/*     */   private synchronized void trim()
/*     */   {
/* 128 */     while (this.mCurrentSize > this.mSizeLimit) {
/* 129 */       byte[] buf = (byte[])this.mBuffersByLastUse.remove(0);
/* 130 */       this.mBuffersBySize.remove(buf);
/* 131 */       this.mCurrentSize -= buf.length;
/*     */     }
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.ByteArrayPool
 * JD-Core Version:    0.5.4
 */