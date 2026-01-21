package com.example.calendarapplication.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarapplication.R;
import com.example.calendarapplication.db.UserDAO;
import com.example.calendarapplication.model.User;
import com.example.calendarapplication.util.PasswordUtils;

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivBack;
    private EditText etOriginalPassword, etNewPassword, etConfirmNewPassword;
    private TextView tvConfirm;

    private UserDAO userDAO;
    private String currentPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        initViews();
        userDAO = new UserDAO(this);
        currentPhone = getCurrentUserPhone();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        etOriginalPassword = findViewById(R.id.et_original_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmNewPassword = findViewById(R.id.et_confirm_new_password);
        tvConfirm = findViewById(R.id.tv_confirm);

        ivBack.setOnClickListener(this);
        tvConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.tv_confirm) {
            changePassword();
        }
    }

    private void changePassword() {
        String originalPassword = etOriginalPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmNewPassword = etConfirmNewPassword.getText().toString().trim();

        if (originalPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证原密码是否正确（使用加密验证）
        User user = userDAO.queryUserByPhone(currentPhone);
        if (user == null || !PasswordUtils.verifyPassword(originalPassword, user.getSalt(), user.getPassword())) {
            Toast.makeText(this, "原密码错误", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证原密码和新密码不能一样
        if (originalPassword.equals(newPassword)) {
            Toast.makeText(this, "原密码和新密码不能一样", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证新密码和确认新密码是否一致
        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(this, "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新密码（使用加密方法）
        int rows = userDAO.updatePassword(currentPhone, newPassword);
        if (rows > 0) {
            Toast.makeText(this, "密码修改成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "密码修改失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取当前登录用户的手机号
     */
    private String getCurrentUserPhone() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        return sharedPreferences.getString("phone", null);
    }
}