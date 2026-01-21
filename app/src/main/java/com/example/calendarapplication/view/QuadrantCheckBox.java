package com.example.calendarapplication.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatCheckBox;

import com.example.calendarapplication.R;

/**
 * 四象限复选框：根据象限类型显示不同颜色的圆形，选中时显示勾选状态
 */
public class QuadrantCheckBox extends AppCompatCheckBox {
    // 四象限类型（0-3分别对应不同优先级/象限）
    private int quadrantType;

    public QuadrantCheckBox(Context context) {
        super(context);
        init(null);
    }

    public QuadrantCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public QuadrantCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * 初始化属性和资源
     */
    private void init(AttributeSet attrs) {
        // 从XML属性中获取象限类型（默认0：重要紧急）
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.QuadrantCheckBox);
            quadrantType = ta.getInt(R.styleable.QuadrantCheckBox_quadrantType, 0);
            ta.recycle();
        }
        
        // 设置对应的checkbox_quadrant资源
        updateCheckBoxDrawable();
    }

    /**
     * 更新复选框的背景资源（根据象限类型）
     */
    private void updateCheckBoxDrawable() {
        // 确保象限类型在有效范围内（0-3）
        if (quadrantType < 0 || quadrantType > 3) {
            quadrantType = 0;
        }
        
        // 根据象限类型设置对应的checkbox_quadrant资源
        int drawableResId;
        switch (quadrantType) {
            case 0:
                drawableResId = R.drawable.checkbox_quadrant_0;
                break;
            case 1:
                drawableResId = R.drawable.checkbox_quadrant_1;
                break;
            case 2:
                drawableResId = R.drawable.checkbox_quadrant_2;
                break;
            case 3:
                drawableResId = R.drawable.checkbox_quadrant_3;
                break;
            default:
                drawableResId = R.drawable.checkbox_quadrant_0;
        }
        
        // 设置背景资源
        super.setButtonDrawable(drawableResId);
    }

    /**
     * 设置象限类型并更新UI
     * @param type 象限类型 (0-3)
     */
    public void setQuadrantType(int type) {
        this.quadrantType = type;
        updateCheckBoxDrawable();
    }

    /**
     * 获取当前象限类型
     * @return 当前象限类型
     */
    public int getQuadrantType() {
        return quadrantType;
    }
}