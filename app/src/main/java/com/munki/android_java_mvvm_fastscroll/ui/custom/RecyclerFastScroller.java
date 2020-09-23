package com.munki.android_java_mvvm_fastscroll.ui.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

import com.munki.android_java_mvvm_fastscroll.R;
import com.munki.android_java_mvvm_fastscroll.databinding.ItemFastScrollerBinding;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

/**
 * RecyclerFastScroller
 * @author 나비이쁜이
 * @since 2020.09.22
 */
public class RecyclerFastScroller extends LinearLayout {

    // Context & Binding & ScrollListener
    private Context mContext;
    private ItemFastScrollerBinding mBinding;
    private OnFastScrollListener onFastScrollListener = null;

    // Recyclerview & Height & ObjectAnimator
    private int scrollerHeight = 0;
    private RecyclerView recyclerView = null;
    private ObjectAnimator currentAnimator = null, bubbleAnimator = null;

    // Word Sections
    private static String[] bubbleList = null;
    private static LinkedHashMap<String, Integer> HandleMap = null;

    // Setting Options
    private float handleRadius;
    private int handleMargin, handleWidth;

    // Bubble Option
    private static final long BUBBLE_ANIMATION_DURATION = 500;
    private static final int TRACK_SNAP_RANGE = 5;

    // Canvas RectF -> Float 값을 지정하는 그래픽s 클래스
    private RectF handlePositionRect;

    // Canvas Paint -> 그리기 도구
    private Paint handleBackgroundPaint, handleTextPaint;

    /************************************************************************************************************************************************/

    // - 생성자
    public RecyclerFastScroller(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init(attrs);
    }

    // - 생성자
    public RecyclerFastScroller(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(attrs);
    }

    // - Init
    private void init(AttributeSet attrs) {
        // View - DataBinding
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBinding = DataBindingUtil.inflate(layoutInflater, R.layout.item_fast_scroller, this, true);

        // 가로 방향 설정 & 범위 설정 OFF
        this.setOrientation(HORIZONTAL);
        this.setClipChildren(false);

        // init
        onFastScrollListener = new OnFastScrollListener();
        bubbleList = new String[]{};
        HandleMap = new LinkedHashMap<>();

        // RectF init
        handlePositionRect = new RectF();

        // Paint init
        handleTextPaint = new Paint();
        handleBackgroundPaint = new Paint();

        // Ant Alias -> 안티얼레이징 -> 색상차가 뚜렷한 경계 부근에 중간색을 삽입하여 도형이나 글꼴이 주변 배경과 부드럽게 잘 어울리도록 하는 기법 (feat.게임)
        handleBackgroundPaint.setAntiAlias(true);

        // Values - Attrs (Xml Option)
        TypedArray array = mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.FastScrollSection, 0, 0);

        // Values - Attrs value - Default Value (지정하지 않은다면 기본값 설정)
        int handleBackgroundColor = array.getColor(R.styleable.FastScrollSection_handleBackgroundColor, 0xffffffff);
        int handleTextColor = array.getColor(R.styleable.FastScrollSection_handleTextColor, 0xff000000);
        float handleRadius = array.getFloat(R.styleable.FastScrollSection_handleRadius, 60f);
        int handleWidth = array.getInt(R.styleable.FastScrollSection_handleWidth, 20);
        int handleMargin = array.getInt(R.styleable.FastScrollSection_handleMargin, 0);

        // Setter - Handle
        setHandleBackgroundColor(handleBackgroundColor);
        setHandleTextColor(handleTextColor);
        setHandleRadius(handleRadius);
        setHandleWidth(handleWidth);
        setHandleMargin(handleMargin);

        // recycle -> Remove | 초기 화면에 Alpha값을 초기화 하기 위해서 0으로 지정
        this.setAlpha(0f);
        array.recycle();
    }

    /************************************************************************************************************************************************/

    // Override

    /**
     * onSizeChanged
     */
    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        scrollerHeight = height;
    }

    /**
     * onDetachedFromWindow
     */
    @Override
    protected void onDetachedFromWindow() {
        recyclerView.removeOnScrollListener(onFastScrollListener);
        super.onDetachedFromWindow();
    }

    /**
     * onTouchEvent
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                showHandle(true);

                if (event.getX() < mBinding.ivHandle.getX() - ViewCompat.getPaddingStart(mBinding.ivHandle))
                    return false;

                if (currentAnimator != null)
                    currentAnimator.cancel();

                if (mBinding.tvBubble.getVisibility() == View.GONE)
                    showBubble(true);

                mBinding.ivHandle.setSelected(true);

                float down_y = event.getY();
                setFastScrollerPosition(down_y);
                setRecyclerViewPosition(down_y);
                return true;

            case MotionEvent.ACTION_MOVE:
                showHandle(true);
                float move_y = event.getY();
                setFastScrollerPosition(move_y);
                setRecyclerViewPosition(move_y);
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                showHandle(false);
                mBinding.ivHandle.setSelected(false);
                showBubble(false);
                return true;

            default:
                return super.onTouchEvent(event);
        }
    }

    /**
     * dispatchDraw
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // section이 없는 경우 return
        if (bubbleList.length == 0)
            return;

        float handle_density_Width = handleWidth * getDensity();
        float handle_density_width_compensation = handleWidth * getDensity();
        float handle_density_margin = this.handleMargin * getDensity();

        // 좌측 공간 -> Handle 공간을 제외한 Bubble이 표현될 모든 공간
        float leftBubblePosition = this.getWidth() - this.getPaddingRight() - handle_density_Width;

        // Handle 사각형 범위 (Radius 제외)
        handlePositionRect.left = leftBubblePosition;
        handlePositionRect.right = leftBubblePosition + handle_density_Width;
        handlePositionRect.top = this.getPaddingTop();
        handlePositionRect.bottom = this.getHeight() - this.getPaddingBottom();

        // Handle Draw -> Rect / x축 radius / y축 radius / Paint
        canvas.drawRoundRect(handlePositionRect, handleRadius, handleRadius, handleBackgroundPaint);

        // 상하 패딩을 제외한 값에 bubble 리스트 크기만큼 나눈 사이즈
        int handleSize = (this.getHeight() - this.getPaddingTop() - getPaddingBottom()) / bubbleList.length - 1;

        // Handle Text Size
        handleTextPaint.setTextSize(handle_density_Width / 2);

        // Handle Text Draw
        for (int i = 0; i < bubbleList.length; i++) {
            float x = (float) (leftBubblePosition + (handleTextPaint.getTextSize() / 1.5));
            float y = this.getHeight() - (handle_density_width_compensation + (handleSize * i)) > 100
                    ? handle_density_width_compensation + getPaddingTop() + handle_density_margin + (handleSize * i)
                    : handle_density_width_compensation + getPaddingTop() + (handleSize * i);

            canvas.drawText(bubbleList[i].toUpperCase(), x, y, handleTextPaint);
        }
    }

    /************************************************************************************************************************************************/

    // Public Method

    /**
     * setRecyclerView
     */
    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        this.recyclerView.addOnScrollListener(onFastScrollListener);
    }

    /**
     * setKeywordList
     */
    public void setKeywordList(ArrayList<String> keywordList) {
        Collections.sort(keywordList, RecyclerFastScroller.OrderingByKorean.getComparator());

        for (int i = 0; i < keywordList.size(); i++) {
            String item = keywordList.get(i);
            String index = item.substring(0, 1);

            char c = index.charAt(0);
            if (RecyclerFastScroller.OrderingByKorean.isKorean(c)) {
                index = String.valueOf(RecyclerFastScroller.KoreanChar.getCompatChoseong(c));
            }

            if (HandleMap.get(index) == null)
                HandleMap.put(index, i);
        }

        ArrayList<String> indexList = new ArrayList<>(HandleMap.keySet());
        bubbleList = new String[indexList.size()];

        indexList.toArray(bubbleList);
        indexList.clear();
        indexList.trimToSize();
    }

    /**
     * showHandle
     */
    public void showHandle(boolean show) {
        if (show) {
            if (bubbleAnimator != null)
                bubbleAnimator.cancel();

            this.setAlpha(1f);
        } else {
            bubbleAnimator = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0f).setDuration(BUBBLE_ANIMATION_DURATION);

            if (this.getAlpha() == 1f)
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bubbleAnimator.start();
                    }
                }, 1000);
        }
    }

    /************************************************************************************************************************************************/

    // XML Public Method

    /**
     * Handle Background Color Setting
     */
    public void setHandleBackgroundColor(int colorInt) {
        handleBackgroundPaint.setColor(colorInt);
    }

    /**
     * Handle TexrtColor Setting
     */
    public void setHandleTextColor(int colorInt) {
        handleTextPaint.setColor(colorInt);
    }

    /**
     * Handle Background Radius Setting
     */
    public void setHandleRadius(float radiusInt) {
        this.handleRadius = radiusInt;
    }

    /**
     * Handle Width
     */
    public void setHandleWidth(int widthInt) {
        this.handleWidth = widthInt;
    }

    /**
     * Handle TOP-BOTTOM Margin Setting
     */
    public void setHandleMargin(int marginInt) {
        this.handleMargin = marginInt;
    }

    /************************************************************************************************************************************************/

    // private Method

    /**
     * showBubble
     * true     -> Bubble Show
     * false    -> Bubble Not Show
     */
    private void showBubble(boolean show) {
        if (show) {
            mBinding.tvBubble.setVisibility(View.VISIBLE);

            if (currentAnimator != null)
                currentAnimator.cancel();

            currentAnimator = ObjectAnimator.ofFloat(mBinding.tvBubble, "alpha", 0f, 1f).setDuration(BUBBLE_ANIMATION_DURATION);
        } else {
            if (currentAnimator != null)
                currentAnimator.cancel();

            currentAnimator = ObjectAnimator.ofFloat(mBinding.tvBubble, "alpha", 1f, 0f).setDuration(BUBBLE_ANIMATION_DURATION);
            currentAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    bubbleHide();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    bubbleHide();
                }

                private void bubbleHide() {
                    mBinding.tvBubble.setVisibility(View.GONE);
                    currentAnimator = null;
                }
            });
        }
        currentAnimator.start();
    }

    /**
     * updateFastScrollerPosition
     */
    private void updateFastScrollerPosition() {
        if (mBinding.ivHandle.isSelected())
            return;

        int verticalScrollOffset = this.recyclerView.computeVerticalScrollOffset();
        int verticalScrollRange = this.recyclerView.computeVerticalScrollRange();

        float proportion = (float) verticalScrollOffset / (float) verticalScrollRange;
        this.setFastScrollerPosition((float) this.scrollerHeight * proportion);
    }

    /**
     * setFastScrollerPosition
     */
    private void setFastScrollerPosition(float y) {
        int handleHeight = mBinding.ivHandle.getHeight();
        mBinding.ivHandle.setY(getValueInRange(0, this.scrollerHeight - handleHeight, (int) (y - (float) (handleHeight / 2))));
    }

    /**
     * setRecyclerViewPosition
     */
    private void setRecyclerViewPosition(float y) {
        int itemCount = recyclerView.getAdapter().getItemCount();
        float proportion = 0f;

        if (mBinding.ivHandle.getY() == 0f)
            proportion = 0f;
        else if (mBinding.ivHandle.getY() + mBinding.ivHandle.getHeight() >= scrollerHeight - TRACK_SNAP_RANGE)
            proportion = 1f;
        else
            proportion = y / (float) scrollerHeight;

        // to Recyclerview Move Position
        int targetPosition = (int) getValueInRange(0, itemCount - 1, (int) (proportion * itemCount));
        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(targetPosition, 0);

        // Bubble setText
        String bubbleText = ((FastScrollable) recyclerView.getAdapter()).setBubbleText(targetPosition);
        mBinding.tvBubble.setText(bubbleText);
    }

    /**
     * getValueInRange
     */
    private float getValueInRange(int min, int max, int adjust) {
        int minimum = Math.max(min, adjust);
        return (float) Math.min(minimum, max);
    }

    /**
     * getDensity
     */
    private float getDensity() {
        return mContext.getResources().getDisplayMetrics().density;
    }

    /************************************************************************************************************************************************/

    // Adapter, Listener, Interfaces

    // - KoreanIndexerRecyclerAdapter
    public abstract static class KoreanIndexerRecyclerAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> implements SectionIndexer {
        @Override
        public Object[] getSections() {
            return bubbleList;
        }

        @Override
        public int getPositionForSection(int section) {
            return HandleMap.get(bubbleList[section]);
        }

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }
    }

    /**
     * OnFastScrollListener
     */
    public class OnFastScrollListener extends OnScrollListener {

        public OnFastScrollListener() {
            super();
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            updateFastScrollerPosition();
        }
    }

    /**
     * FastScrollable
     */
    public interface FastScrollable {
        String setBubbleText(int position);
    }

    /************************************************************************************************************************************************/

    // Korean Ordering

    // - Korean - https://github.com/bangjunyoung/KoreanTextMatcher
    private static class KoreanChar {

        private static final int CHOSEONG_COUNT = 19;
        private static final int JUNGSEONG_COUNT = 21;
        private static final int JONGSEONG_COUNT = 28;
        private static final int HANGUL_SYLLABLE_COUNT = CHOSEONG_COUNT * JUNGSEONG_COUNT * JONGSEONG_COUNT;
        private static final int HANGUL_SYLLABLES_BASE = 0xAC00;
        private static final int HANGUL_SYLLABLES_END = HANGUL_SYLLABLES_BASE + HANGUL_SYLLABLE_COUNT;

        private static final int[] COMPAT_CHOSEONG_MAP = new int[]{
                0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139, 0x3141, 0x3142, 0x3143, 0x3145,
                0x3146, 0x3147, 0x3148, 0x3149, 0x314A, 0x314B, 0x314C, 0x314D, 0x314E
        };

        private KoreanChar() {
            // Can never be instantiated.
        }

        private static boolean isSyllable(char c) {
            return HANGUL_SYLLABLES_BASE <= c && c < HANGUL_SYLLABLES_END;
        }

        private static char getCompatChoseong(char value) {
            if (!isSyllable(value))
                return '\0';

            final int choseongIndex = getChoseongIndex(value);
            return (char) COMPAT_CHOSEONG_MAP[choseongIndex];
        }

        private static int getChoseongIndex(char syllable) {
            final int syllableIndex = syllable - HANGUL_SYLLABLES_BASE;
            return syllableIndex / (JUNGSEONG_COUNT * JONGSEONG_COUNT);
        }
    }

    // - Order - http://reimaginer.tistory.com/entry/한글영어특수문자-순-정렬하는-java-compare-메서드-만들기
    private static class OrderingByKorean {
        private static final int REVERSE = -1;
        private static final int LEFT_FIRST = -1;
        private static final int RIGHT_FIRST = 1;

        private static Comparator<String> getComparator() {
            return new Comparator<String>() {
                public int compare(String left, String right) {
                    return RecyclerFastScroller.OrderingByKorean.compare(left, right);
                }
            };
        }

        private static int compare(String left, String right) {

            left = StringUtils.upperCase(left).replaceAll(" ", "");
            right = StringUtils.upperCase(right).replaceAll(" ", "");

            int leftLen = left.length();
            int rightLen = right.length();
            int minLen = Math.min(leftLen, rightLen);

            for (int i = 0; i < minLen; ++i) {
                char leftChar = left.charAt(i);
                char rightChar = right.charAt(i);

                if (leftChar != rightChar) {
                    if (isKoreanAndEnglish(leftChar, rightChar) || isKoreanAndNumber(leftChar, rightChar)
                            || isEnglishAndNumber(leftChar, rightChar) || isKoreanAndSpecial(leftChar, rightChar)) {
                        return (leftChar - rightChar) * REVERSE;
                    } else if (isEnglishAndSpecial(leftChar, rightChar) || isNumberAndSpecial(leftChar, rightChar)) {
                        if (isEnglish(leftChar) || isNumber(leftChar)) {
                            return LEFT_FIRST;
                        } else {
                            return RIGHT_FIRST;
                        }
                    } else {
                        return leftChar - rightChar;
                    }
                }
            }

            return leftLen - rightLen;
        }

        private static boolean isKoreanAndEnglish(char ch1, char ch2) {
            return (isEnglish(ch1) && isKorean(ch2)) || (isKorean(ch1) && isEnglish(ch2));
        }

        private static boolean isKoreanAndNumber(char ch1, char ch2) {
            return (isNumber(ch1) && isKorean(ch2)) || (isKorean(ch1) && isNumber(ch2));
        }

        private static boolean isEnglishAndNumber(char ch1, char ch2) {
            return (isNumber(ch1) && isEnglish(ch2)) || (isEnglish(ch1) && isNumber(ch2));
        }

        private static boolean isKoreanAndSpecial(char ch1, char ch2) {
            return (isKorean(ch1) && isSpecial(ch2)) || (isSpecial(ch1) && isKorean(ch2));
        }

        private static boolean isEnglishAndSpecial(char ch1, char ch2) {
            return (isEnglish(ch1) && isSpecial(ch2)) || (isSpecial(ch1) && isEnglish(ch2));
        }

        private static boolean isNumberAndSpecial(char ch1, char ch2) {
            return (isNumber(ch1) && isSpecial(ch2)) || (isSpecial(ch1) && isNumber(ch2));
        }

        private static boolean isEnglish(char ch) {
            return (ch >= (int) 'A' && ch <= (int) 'Z') || (ch >= (int) 'a' && ch <= (int) 'z');
        }

        private static boolean isKorean(char ch) {
            return ch >= Integer.parseInt("AC00", 16) && ch <= Integer.parseInt("D7A3", 16);
        }

        private static boolean isNumber(char ch) {
            return ch >= (int) '0' && ch <= (int) '9';
        }

        private static boolean isSpecial(char ch) {
            return (ch >= (int) '!' && ch <= (int) '/')         // !"#$%&'()*+,-./
                    || (ch >= (int) ':' && ch <= (int) '@')     // :;<=>?@
                    || (ch >= (int) '[' && ch <= (int) '`')     // [\]^_`
                    || (ch >= (int) '{' && ch <= (int) '~');    // {|}~
        }
    }
}
