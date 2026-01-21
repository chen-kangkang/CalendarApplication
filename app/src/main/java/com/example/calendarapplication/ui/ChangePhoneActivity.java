package com.example.calendarapplication.ui;

import android.content.Context;
import android.content.Intent;
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

public class ChangePhoneActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivBack;
    private EditText etOriginalPhone, etPassword, etNewPhone;
    private TextView tvConfirm;

    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_phone);
        initViews();
        userDAO = new UserDAO(this);
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        etOriginalPhone = findViewById(R.id.et_original_phone);
        etPassword = findViewById(R.id.et_password);
        etNewPhone = findViewById(R.id.et_new_phone);
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
            changePhone();
        }
    }

    private void changePhone() {
        String originalPhone = etOriginalPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String newPhone = etNewPhone.getText().toString().trim();

        if (originalPhone.isEmpty() || password.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证原手机号和密码是否正确
        User user = userDAO.queryUserByPhone(originalPhone);
        if (user == null || !user.getPassword().equals(password)) {
            Toast.makeText(this, "原手机号或密码错误", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证新手机号是否已被使用
        User existingUser = userDAO.queryUserByPhone(newPhone);
        if (existingUser != null && !existingUser.getPhone().equals(originalPhone)) {
            Toast.makeText(this, "新手机号已被注册", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新手机号
        user.setPhone(newPhone);
        int rows = userDAO.updateUser(user);
        if (rows > 0) {
            // 更新SharedPreferences中的当前用户手机号
            SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("phone", newPhone);
            editor.apply();

            Toast.makeText(this, "手机号修改成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "手机号修改失败", Toast.LENGTH_SHORT).show();
        }
    }
}