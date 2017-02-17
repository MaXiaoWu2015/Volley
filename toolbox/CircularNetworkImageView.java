/*    */ package com.android.volley.toolbox;
/*    */ 
/*    */ import android.content.Context;
/*    */ import android.graphics.Bitmap;
/*    */ import android.graphics.Bitmap.Config;
/*    */ import android.graphics.Canvas;
/*    */ import android.graphics.Paint;
/*    */ import android.graphics.PorterDuff.Mode;
/*    */ import android.graphics.PorterDuffXfermode;
/*    */ import android.graphics.Rect;
/*    */ import android.graphics.RectF;
/*    */ import android.graphics.drawable.BitmapDrawable;
/*    */ import android.util.AttributeSet;
/*    */ 
/*    */ public class CircularNetworkImageView extends NetworkImageView
/*    */ {
/*    */   Context mContext;
/*    */ 
/*    */   public CircularNetworkImageView(Context context)
/*    */   {
/* 21 */     super(context);
/* 22 */     this.mContext = context;
/*    */   }
/*    */ 
/*    */   public CircularNetworkImageView(Context context, AttributeSet attrs) {
/* 26 */     this(context, attrs, 0);
/* 27 */     this.mContext = context;
/*    */   }
/*    */ 
/*    */   public CircularNetworkImageView(Context context, AttributeSet attrs, int defStyle)
/*    */   {
/* 32 */     super(context, attrs, defStyle);
/* 33 */     this.mContext = context;
/*    */   }
/*    */ 
/*    */   public void setImageBitmap(Bitmap bm)
/*    */   {
/* 38 */     if (bm == null) return;
/* 39 */     setImageDrawable(
/* 40 */       new BitmapDrawable(this.mContext.getResources(), 
/* 40 */       getCircularBitmap(bm)));
/*    */   }
/*    */ 
/*    */   public Bitmap getCircularBitmap(Bitmap bitmap)
/*    */   {
/* 51 */     Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), 
/* 52 */       bitmap.getHeight(), Bitmap.Config.ARGB_8888);
/* 53 */     Canvas canvas = new Canvas(output);
/* 54 */     int width = bitmap.getWidth();
/* 55 */     if (bitmap.getWidth() > bitmap.getHeight())
/* 56 */       width = bitmap.getHeight();
/* 57 */     int color = -12434878;
/* 58 */     Paint paint = new Paint();
/* 59 */     Rect rect = new Rect(0, 0, width, width);
/* 60 */     RectF rectF = new RectF(rect);
/* 61 */     float roundPx = width / 2;
/*    */ 
/* 63 */     paint.setAntiAlias(true);
/* 64 */     canvas.drawARGB(0, 0, 0, 0);
/* 65 */     paint.setColor(-12434878);
/* 66 */     canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
/*    */ 
/* 68 */     paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
/* 69 */     canvas.drawBitmap(bitmap, rect, rect, paint);
/*    */ 
/* 71 */     return output;
/*    */   }
/*    */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.CircularNetworkImageView
 * JD-Core Version:    0.5.4
 */