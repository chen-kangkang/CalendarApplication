package com.example.calendarapplication.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarapplication.R;
import com.example.calendarapplication.db.UserDAO;
import com.example.calendarapplication.model.User;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";

    private EditText etPhone;
    private EditText etPassword;
    private TextView tvPhoneError;
    private TextView tvPasswordError;
    private Button btnLogin;

    private UserDAO userDAO;

    // 正则表达式：手机号验证
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";
    // 正则表达式：密码验证（6-12位非空数字或字母或特殊符号）
    private static final String PASSWORD_PATTERN = "^[\\S]{6,12}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化UI组件
        initViews();
        
        // 初始化数据库操作对象
        userDAO = new UserDAO(this);

        // 检查是否已经登录（3天免登录）
        if (checkAutoLogin()) {
            // 直接跳转到首页
            gotoMainActivity();
        }
    }

    private void initViews() {
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        tvPhoneError = findViewById(R.id.tv_phone_error);
        tvPasswordError = findViewById(R.id.tv_password_error);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(this);
        
        // 添加失去焦点监听器，实时验证输入格式
        etPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validatePhone();
            }
        });
        
        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validatePassword();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_login) {
            // 验证手机号和密码
            if (validateInputs()) {
                // 执行登录/注册操作
                loginOrRegister();
            }
        }
    }

    // 验证手机号
    private boolean validatePhone() {
        boolean isValid = true;
        String phone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            tvPhoneError.setText("手机号不能为空");
            tvPhoneError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!Pattern.matches(PHONE_PATTERN, phone)) {
            tvPhoneError.setText("手机号格式不正确");
            tvPhoneError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvPhoneError.setVisibility(View.GONE);
        }
        return isValid;
    }
    
    // 验证密码
    private boolean validatePassword() {
        boolean isValid = true;
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            tvPasswordError.setText("密码不能为空");
            tvPasswordError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!Pattern.matches(PASSWORD_PATTERN, password)) {
            tvPasswordError.setText("密码格式不正确（6-12位非空字符）");
            tvPasswordError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvPasswordError.setVisibility(View.GONE);
        }
        return isValid;
    }
    
    // 验证所有输入
    private boolean validateInputs() {
        boolean isValid = true;
        isValid = validatePhone() && isValid;
        isValid = validatePassword() && isValid;
        return isValid;
    }

    // 执行登录/注册操作
    private void loginOrRegister() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 查询用户是否存在
        User user = userDAO.queryUserByPhone(phone);

        if (user == null) {
            // 用户不存在，执行注册
            User newUser = new User(phone, password);
            long id = userDAO.registerUser(newUser);

            if (id > 0) {
                // 注册成功
                Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
                // 设置自动登录
                setAutoLogin(phone, id);
                // 跳转到首页
                gotoMainActivity();
            } else {
                // 注册失败
                Toast.makeText(this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 用户存在，执行登录验证
            if (userDAO.login(phone, password)) {
                // 登录成功
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                // 设置自动登录
                setAutoLogin(phone, user.getId());
                // 跳转到首页
                gotoMainActivity();
            } else {
                // 登录失败
                Toast.makeText(this, "密码不正确，请重试", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 设置自动登录
    private void setAutoLogin(String phone, long userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // 存储手机号
        editor.putString("phone", phone);
        // 存储用户ID
        editor.putLong("user_id", userId);
        // 存储登录时间
        editor.putLong("login_time", System.currentTimeMillis());
        editor.apply();
    }

    // 检查是否自动登录
    private boolean checkAutoLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String phone = sharedPreferences.getString("phone", null);
        long loginTime = sharedPreferences.getLong("login_time", 0);

        if (phone != null && loginTime != 0) {
            // 计算登录时间和当前时间的差值（毫秒）
            long currentTime = System.currentTimeMillis();
            long diffTime = currentTime - loginTime;
            // 3天的毫秒数：3 * 24 * 60 * 60 * 1000 = 259200000
            if (diffTime < 3 * 24 * 60 * 60 * 1000) {
                // 3天内免登录
                return true;
            }
        }
        return false;
    }

    // 跳转到首页
    private void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}