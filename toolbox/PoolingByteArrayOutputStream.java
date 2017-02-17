/*    */ package com.android.volley.toolbox;
/*    */ 
/*    */ import java.io.ByteArrayOutputStream;
/*    */ import java.io.IOException;
/*    */ 
/*    */ public class PoolingByteArrayOutputStream extends ByteArrayOutputStream
/*    */ {
/*    */   private static final int DEFAULT_SIZE = 256;
/*    */   private final ByteArrayPool mPool;
/*    */ 
/*    */   public PoolingByteArrayOutputStream(ByteArrayPool pool)
/*    */   {
/* 40 */     this(pool, 256);
/*    */   }
/*    */ 
/*    */   public PoolingByteArrayOutputStream(ByteArrayPool pool, int size)
/*    */   {
/* 52 */     this.mPool = pool;
/* 53 */     this.buf = this.mPool.getBuf(Math.max(size, 256));
/*    */   }
/*    */ 
/*    */   public void close() throws IOException
/*    */   {
/* 58 */     this.mPool.returnBuf(this.buf);
/* 59 */     this.buf = null;
/* 60 */     super.close();
/*    */   }
/*    */ 
/*    */   public void finalize()
/*    */   {
/* 65 */     this.mPool.returnBuf(this.buf);
/*    */   }
/*    */ 
/*    */   private void expand(int i)
/*    */   {
/* 73 */     if (this.count + i <= this.buf.length) {
/* 74 */       return;
/*    */     }
/* 76 */     byte[] newbuf = this.mPool.getBuf((this.count + i) * 2);
/* 77 */     System.arraycopy(this.buf, 0, newbuf, 0, this.count);
/* 78 */     this.mPool.returnBuf(this.buf);
/* 79 */     this.buf = newbuf;
/*    */   }
/*    */ 
/*    */   public synchronized void write(byte[] buffer, int offset, int len)
/*    */   {
/* 84 */     expand(len);
/* 85 */     super.write(buffer, offset, len);
/*    */   }
/*    */ 
/*    */   public synchronized void write(int oneByte)
/*    */   {
/* 90 */     expand(1);
/* 91 */     super.write(oneByte);
/*    */   }
/*    */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.PoolingByteArrayOutputStream
 * JD-Core Version:    0.5.4
 */