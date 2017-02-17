/*     */ package com.android.volley.toolbox;
/*     */ 
/*     */ import android.os.SystemClock;
/*     */ import com.android.volley.Cache;
/*     */ import com.android.volley.Cache.Entry;
/*     */ import com.android.volley.VolleyLog;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.EOFException;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.FilterInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Collections;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class DiskBasedCache
/*     */   implements Cache
/*     */ {
/*  47 */   private final Map<String, CacheHeader> mEntries = new LinkedHashMap(16, 0.75F, true);
/*     */ 
/*  50 */   private long mTotalSize = 0L;
/*     */   private final File mRootDirectory;
/*     */   private final int mMaxCacheSizeInBytes;
/*     */   private static final int DEFAULT_DISK_USAGE_BYTES = 5242880;
/*     */   private static final float HYSTERESIS_FACTOR = 0.9F;
/*     */   private static final int CACHE_MAGIC = 538247942;
/*     */ 
/*     */   public DiskBasedCache(File rootDirectory, int maxCacheSizeInBytes)
/*     */   {
/*  73 */     this.mRootDirectory = rootDirectory;
/*  74 */     this.mMaxCacheSizeInBytes = maxCacheSizeInBytes;
/*     */   }
/*     */ 
/*     */   public DiskBasedCache(File rootDirectory)
/*     */   {
/*  83 */     this(rootDirectory, 5242880);
/*     */   }
/*     */ 
/*     */   public synchronized void clear()
/*     */   {
/*  91 */     File[] files = this.mRootDirectory.listFiles();
/*  92 */     if (files != null) {
/*  93 */       for (File file : files) {
/*  94 */         file.delete();
/*     */       }
/*     */     }
/*  97 */     this.mEntries.clear();
/*  98 */     this.mTotalSize = 0L;
/*  99 */     VolleyLog.d("Cache cleared.", new Object[0]);
/*     */   }
/*     */ 
/*     */   public synchronized Cache.Entry get(String key)
/*     */   {
/* 107 */     CacheHeader entry = (CacheHeader)this.mEntries.get(key);
/*     */ 
/* 109 */     if (entry == null) {
/* 110 */       return null;
/*     */     }
/*     */ 
/* 113 */     File file = getFileForKey(key);
/* 114 */     CountingInputStream cis = null;
/*     */     try {
/* 116 */       cis = new CountingInputStream(new FileInputStream(file), null);
/* 117 */       CacheHeader.readHeader(cis);
/* 118 */       byte[] data = streamToBytes(cis, (int)(file.length() - cis.bytesRead));
/* 119 */       return entry.toCacheEntry(data);
/*     */     } catch (IOException e) {
/* 121 */       VolleyLog.d("%s: %s", new Object[] { file.getAbsolutePath(), e.toString() });
/* 122 */       remove(key);
/* 123 */       return null;
/*     */     } finally {
/* 125 */       if (cis != null)
/*     */         try {
/* 127 */           cis.close();
/*     */         } catch (IOException ioe) {
/* 129 */           return null;
/*     */         }
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void initialize()
/*     */   {
/* 141 */     if (!this.mRootDirectory.exists()) {
/* 142 */       if (!this.mRootDirectory.mkdirs()) {
/* 143 */         VolleyLog.e("Unable to create cache dir %s", new Object[] { this.mRootDirectory.getAbsolutePath() });
/*     */       }
/* 145 */       return;
/*     */     }
/*     */ 
/* 148 */     File[] files = this.mRootDirectory.listFiles();
/* 149 */     if (files == null) {
/* 150 */       return;
/*     */     }
/* 152 */     for (File file : files) {
/* 153 */       BufferedInputStream fis = null;
/*     */       try {
/* 155 */         fis = new BufferedInputStream(new FileInputStream(file));
/* 156 */         CacheHeader entry = CacheHeader.readHeader(fis);
/* 157 */         entry.size = file.length();
/* 158 */         putEntry(entry.key, entry);
/*     */       } catch (IOException e) {
/* 160 */         if (file != null)
/* 161 */           file.delete();
/*     */       }
/*     */       finally {
/*     */         try {
/* 165 */           if (fis != null)
/* 166 */             fis.close();
/*     */         }
/*     */         catch (IOException localIOException2)
/*     */         {
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void invalidate(String key, boolean fullExpire)
/*     */   {
/* 180 */     Cache.Entry entry = get(key);
/* 181 */     if (entry != null) {
/* 182 */       entry.softTtl = 0L;
/* 183 */       if (fullExpire) {
/* 184 */         entry.ttl = 0L;
/*     */       }
/* 186 */       put(key, entry);
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void put(String key, Cache.Entry entry)
/*     */   {
/* 196 */     pruneIfNeeded(entry.data.length);
/* 197 */     File file = getFileForKey(key);
/*     */     try {
/* 199 */       FileOutputStream fos = new FileOutputStream(file);
/* 200 */       CacheHeader e = new CacheHeader(key, entry);
/* 201 */       boolean success = e.writeHeader(fos);
/* 202 */       if (!success) {
/* 203 */         fos.close();
/* 204 */         VolleyLog.d("Failed to write header for %s", new Object[] { file.getAbsolutePath() });
/* 205 */         throw new IOException();
/*     */       }
/* 207 */       fos.write(entry.data);
/* 208 */       fos.close();
/* 209 */       putEntry(key, e);
/* 210 */       return;
/*     */     }
/*     */     catch (IOException deleted) {
/* 213 */       boolean deleted = file.delete();
/* 214 */       if (!deleted)
/* 215 */         VolleyLog.d("Could not clean up file %s", new Object[] { file.getAbsolutePath() });
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void remove(String key)
/*     */   {
/* 224 */     boolean deleted = getFileForKey(key).delete();
/* 225 */     removeEntry(key);
/* 226 */     if (!deleted)
/* 227 */       VolleyLog.d("Could not delete cache entry for key=%s, filename=%s", new Object[] { 
/* 228 */         key, getFilenameForKey(key) });
/*     */   }
/*     */ 
/*     */   private String getFilenameForKey(String key)
/*     */   {
/* 238 */     int firstHalfLength = key.length() / 2;
/* 239 */     String localFilename = String.valueOf(key.substring(0, firstHalfLength).hashCode());
/* 240 */     localFilename = localFilename + String.valueOf(key.substring(firstHalfLength).hashCode());
/* 241 */     return localFilename;
/*     */   }
/*     */ 
/*     */   public File getFileForKey(String key)
/*     */   {
/* 248 */     return new File(this.mRootDirectory, getFilenameForKey(key));
/*     */   }
/*     */ 
/*     */   private void pruneIfNeeded(int neededSpace)
/*     */   {
/* 256 */     if (this.mTotalSize + neededSpace < this.mMaxCacheSizeInBytes) {
/* 257 */       return;
/*     */     }
/* 259 */     if (VolleyLog.DEBUG) {
/* 260 */       VolleyLog.v("Pruning old cache entries.", new Object[0]);
/*     */     }
/*     */ 
/* 263 */     long before = this.mTotalSize;
/* 264 */     int prunedFiles = 0;
/* 265 */     long startTime = SystemClock.elapsedRealtime();
/*     */ 
/* 267 */     Iterator iterator = this.mEntries.entrySet().iterator();
/* 268 */     while (iterator.hasNext()) {
/* 269 */       Map.Entry entry = (Map.Entry)iterator.next();
/* 270 */       CacheHeader e = (CacheHeader)entry.getValue();
/* 271 */       boolean deleted = getFileForKey(e.key).delete();
/* 272 */       if (deleted)
/* 273 */         this.mTotalSize -= e.size;
/*     */       else {
/* 275 */         VolleyLog.d("Could not delete cache entry for key=%s, filename=%s", new Object[] { 
/* 276 */           e.key, getFilenameForKey(e.key) });
/*     */       }
/* 278 */       iterator.remove();
/* 279 */       ++prunedFiles;
/*     */ 
/* 281 */       if ((float)(this.mTotalSize + neededSpace) < this.mMaxCacheSizeInBytes * 0.9F) {
/*     */         break;
/*     */       }
/*     */     }
/*     */ 
/* 286 */     if (VolleyLog.DEBUG)
/* 287 */       VolleyLog.v("pruned %d files, %d bytes, %d ms", new Object[] { 
/* 288 */         Integer.valueOf(prunedFiles), Long.valueOf(this.mTotalSize - before), Long.valueOf(SystemClock.elapsedRealtime() - startTime) });
/*     */   }
/*     */ 
/*     */   private void putEntry(String key, CacheHeader entry)
/*     */   {
/* 298 */     if (!this.mEntries.containsKey(key)) {
/* 299 */       this.mTotalSize += entry.size;
/*     */     } else {
/* 301 */       CacheHeader oldEntry = (CacheHeader)this.mEntries.get(key);
/* 302 */       this.mTotalSize += entry.size - oldEntry.size;
/*     */     }
/* 304 */     this.mEntries.put(key, entry);
/*     */   }
/*     */ 
/*     */   private void removeEntry(String key)
/*     */   {
/* 311 */     CacheHeader entry = (CacheHeader)this.mEntries.get(key);
/* 312 */     if (entry != null) {
/* 313 */       this.mTotalSize -= entry.size;
/* 314 */       this.mEntries.remove(key);
/*     */     }
/*     */   }
/*     */ 
/*     */   private static byte[] streamToBytes(InputStream in, int length)
/*     */     throws IOException
/*     */   {
/* 322 */     byte[] bytes = new byte[length];
/*     */ 
/* 324 */     int pos = 0;
/*     */     int count;
/* 325 */     while ((pos < length) && ((count = in.read(bytes, pos, length - pos)) != -1))
/*     */     {
/*     */       int count;
/* 326 */       pos += count;
/*     */     }
/* 328 */     if (pos != length) {
/* 329 */       throw new IOException("Expected " + length + " bytes, read " + pos + " bytes");
/*     */     }
/* 331 */     return bytes;
/*     */   }
/*     */ 
/*     */   private static int read(InputStream is)
/*     */     throws IOException
/*     */   {
/* 485 */     int b = is.read();
/* 486 */     if (b == -1) {
/* 487 */       throw new EOFException();
/*     */     }
/* 489 */     return b;
/*     */   }
/*     */ 
/*     */   static void writeInt(OutputStream os, int n) throws IOException {
/* 493 */     os.write(n >> 0 & 0xFF);
/* 494 */     os.write(n >> 8 & 0xFF);
/* 495 */     os.write(n >> 16 & 0xFF);
/* 496 */     os.write(n >> 24 & 0xFF);
/*     */   }
/*     */ 
/*     */   static int readInt(InputStream is) throws IOException {
/* 500 */     int n = 0;
/* 501 */     n |= read(is) << 0;
/* 502 */     n |= read(is) << 8;
/* 503 */     n |= read(is) << 16;
/* 504 */     n |= read(is) << 24;
/* 505 */     return n;
/*     */   }
/*     */ 
/*     */   static void writeLong(OutputStream os, long n) throws IOException {
/* 509 */     os.write((byte)(int)(n >>> 0));
/* 510 */     os.write((byte)(int)(n >>> 8));
/* 511 */     os.write((byte)(int)(n >>> 16));
/* 512 */     os.write((byte)(int)(n >>> 24));
/* 513 */     os.write((byte)(int)(n >>> 32));
/* 514 */     os.write((byte)(int)(n >>> 40));
/* 515 */     os.write((byte)(int)(n >>> 48));
/* 516 */     os.write((byte)(int)(n >>> 56));
/*     */   }
/*     */ 
/*     */   static long readLong(InputStream is) throws IOException {
/* 520 */     long n = 0L;
/* 521 */     n |= (read(is) & 0xFF) << 0;
/* 522 */     n |= (read(is) & 0xFF) << 8;
/* 523 */     n |= (read(is) & 0xFF) << 16;
/* 524 */     n |= (read(is) & 0xFF) << 24;
/* 525 */     n |= (read(is) & 0xFF) << 32;
/* 526 */     n |= (read(is) & 0xFF) << 40;
/* 527 */     n |= (read(is) & 0xFF) << 48;
/* 528 */     n |= (read(is) & 0xFF) << 56;
/* 529 */     return n;
/*     */   }
/*     */ 
/*     */   static void writeString(OutputStream os, String s) throws IOException {
/* 533 */     byte[] b = s.getBytes("UTF-8");
/* 534 */     writeLong(os, b.length);
/* 535 */     os.write(b, 0, b.length);
/*     */   }
/*     */ 
/*     */   static String readString(InputStream is) throws IOException {
/* 539 */     int n = (int)readLong(is);
/* 540 */     byte[] b = streamToBytes(is, n);
/* 541 */     return new String(b, "UTF-8");
/*     */   }
/*     */ 
/*     */   static void writeStringStringMap(Map<String, String> map, OutputStream os) throws IOException {
/* 545 */     if (map != null) {
/* 546 */       writeInt(os, map.size());
/* 547 */       for (Map.Entry entry : map.entrySet()) {
/* 548 */         writeString(os, (String)entry.getKey());
/* 549 */         writeString(os, (String)entry.getValue());
/*     */       }
/*     */     } else {
/* 552 */       writeInt(os, 0);
/*     */     }
/*     */   }
/*     */ 
/*     */   static Map<String, String> readStringStringMap(InputStream is) throws IOException {
/* 557 */     int size = readInt(is);
/* 558 */     Map result = (size == 0) ? 
/* 559 */       Collections.emptyMap() : 
/* 560 */       new HashMap(size);
/* 561 */     for (int i = 0; i < size; ++i) {
/* 562 */       String key = readString(is).intern();
/* 563 */       String value = readString(is).intern();
/* 564 */       result.put(key, value);
/*     */     }
/* 566 */     return result;
/*     */   }
/*     */ 
/*     */   static class CacheHeader
/*     */   {
/*     */     public long size;
/*     */     public String key;
/*     */     public String etag;
/*     */     public long serverDate;
/*     */     public long lastModified;
/*     */     public long ttl;
/*     */     public long softTtl;
/*     */     public Map<String, String> responseHeaders;
/*     */ 
/*     */     private CacheHeader()
/*     */     {
/*     */     }
/*     */ 
/*     */     public CacheHeader(String key, Cache.Entry entry)
/*     */     {
/* 372 */       this.key = key;
/* 373 */       this.size = entry.data.length;
/* 374 */       this.etag = entry.etag;
/* 375 */       this.serverDate = entry.serverDate;
/* 376 */       this.lastModified = entry.lastModified;
/* 377 */       this.ttl = entry.ttl;
/* 378 */       this.softTtl = entry.softTtl;
/* 379 */       this.responseHeaders = entry.responseHeaders;
/*     */     }
/*     */ 
/*     */     public static CacheHeader readHeader(InputStream is)
/*     */       throws IOException
/*     */     {
/* 388 */       CacheHeader entry = new CacheHeader();
/* 389 */       int magic = DiskBasedCache.readInt(is);
/* 390 */       if (magic != 538247942)
/*     */       {
/* 392 */         throw new IOException();
/*     */       }
/* 394 */       entry.key = DiskBasedCache.readString(is);
/* 395 */       entry.etag = DiskBasedCache.readString(is);
/* 396 */       if (entry.etag.equals("")) {
/* 397 */         entry.etag = null;
/*     */       }
/* 399 */       entry.serverDate = DiskBasedCache.readLong(is);
/* 400 */       entry.lastModified = DiskBasedCache.readLong(is);
/* 401 */       entry.ttl = DiskBasedCache.readLong(is);
/* 402 */       entry.softTtl = DiskBasedCache.readLong(is);
/* 403 */       entry.responseHeaders = DiskBasedCache.readStringStringMap(is);
/*     */ 
/* 405 */       return entry;
/*     */     }
/*     */ 
/*     */     public Cache.Entry toCacheEntry(byte[] data)
/*     */     {
/* 412 */       Cache.Entry e = new Cache.Entry();
/* 413 */       e.data = data;
/* 414 */       e.etag = this.etag;
/* 415 */       e.serverDate = this.serverDate;
/* 416 */       e.lastModified = this.lastModified;
/* 417 */       e.ttl = this.ttl;
/* 418 */       e.softTtl = this.softTtl;
/* 419 */       e.responseHeaders = this.responseHeaders;
/* 420 */       return e;
/*     */     }
/*     */ 
/*     */     public boolean writeHeader(OutputStream os)
/*     */     {
/*     */       try
/*     */       {
/* 429 */         DiskBasedCache.writeInt(os, 538247942);
/* 430 */         DiskBasedCache.writeString(os, this.key);
/* 431 */         DiskBasedCache.writeString(os, (this.etag == null) ? "" : this.etag);
/* 432 */         DiskBasedCache.writeLong(os, this.serverDate);
/* 433 */         DiskBasedCache.writeLong(os, this.lastModified);
/* 434 */         DiskBasedCache.writeLong(os, this.ttl);
/* 435 */         DiskBasedCache.writeLong(os, this.softTtl);
/* 436 */         DiskBasedCache.writeStringStringMap(this.responseHeaders, os);
/* 437 */         os.flush();
/* 438 */         return true;
/*     */       } catch (IOException e) {
/* 440 */         VolleyLog.d("%s", new Object[] { e.toString() });
/* 441 */       }return false;
/*     */     }
/*     */   }
/*     */ 
/*     */   private static class CountingInputStream extends FilterInputStream
/*     */   {
/* 448 */     private int bytesRead = 0;
/*     */ 
/*     */     private CountingInputStream(InputStream in) {
/* 451 */       super(in);
/*     */     }
/*     */ 
/*     */     public int read() throws IOException
/*     */     {
/* 456 */       int result = super.read();
/* 457 */       if (result != -1) {
/* 458 */         this.bytesRead += 1;
/*     */       }
/* 460 */       return result;
/*     */     }
/*     */ 
/*     */     public int read(byte[] buffer, int offset, int count) throws IOException
/*     */     {
/* 465 */       int result = super.read(buffer, offset, count);
/* 466 */       if (result != -1) {
/* 467 */         this.bytesRead += result;
/*     */       }
/* 469 */       return result;
/*     */     }
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.DiskBasedCache
 * JD-Core Version:    0.5.4
 */