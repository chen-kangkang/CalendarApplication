package com.example.calendarapplication.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.calendarapplication.R;
import com.example.calendarapplication.db.UserDAO;
import com.example.calendarapplication.model.User;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BasicInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "BasicInfoActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_PHOTO = 2;

    // UI组件
    private ImageView ivBack;
    private RelativeLayout rlAvatar, rlNickname, rlPhone, rlGender, rlAge, rlChangePassword;
    private ImageView ivAvatar;
    private TextView tvNickname, tvPhone, tvGender, tvAge;

    // 数据库操作对象
    private UserDAO userDAO;
    private User currentUser;
    private String currentPhone;

    // 头像文件路径
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_info);

        // 初始化UI组件
        initViews();

        // 初始化数据库操作对象
        userDAO = new UserDAO(this);

        // 获取当前登录用户的手机号
        currentPhone = getCurrentUserPhone();

        // 从数据库获取用户信息并回显
        loadUserInfo();
    }

    private void initViews() {
        // 返回按钮
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);

        // 信息项
        rlAvatar = findViewById(R.id.rl_avatar);
        rlNickname = findViewById(R.id.rl_nickname);
        rlPhone = findViewById(R.id.rl_phone);
        rlGender = findViewById(R.id.rl_gender);
        rlAge = findViewById(R.id.rl_age);
        rlChangePassword = findViewById(R.id.rl_change_password);

        // 头像和信息显示
        ivAvatar = findViewById(R.id.iv_avatar);
        tvNickname = findViewById(R.id.tv_nickname);
        tvPhone = findViewById(R.id.tv_phone);
        tvGender = findViewById(R.id.tv_gender);
        tvAge = findViewById(R.id.tv_age);

        // 设置点击事件（手机号不可修改，不设置点击事件）
        rlAvatar.setOnClickListener(this);
        rlNickname.setOnClickListener(this);
        rlGender.setOnClickListener(this);
        rlAge.setOnClickListener(this);
        rlChangePassword.setOnClickListener(this);
    }

    // 获取当前登录用户的手机号
    private String getCurrentUserPhone() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        return sharedPreferences.getString("phone", null);
    }

    // 从数据库加载用户信息并回显
    private void loadUserInfo() {
        if (currentPhone != null) {
            currentUser = userDAO.queryUserByPhone(currentPhone);
            if (currentUser != null) {
                // 回显用户信息
                updateUserInfoDisplay();
            }
        }
    }

    // 更新用户信息显示
    private void updateUserInfoDisplay() {
        if (currentUser != null) {
            // 设置昵称
            if (currentUser.getNickname() != null && !currentUser.getNickname().isEmpty()) {
                tvNickname.setText(currentUser.getNickname());
            } else {
                tvNickname.setText("我是一个用户昵称");
            }

            // 设置手机号
            tvPhone.setText(currentUser.getPhone());

            // 设置性别
            if (currentUser.getGender() != null && !currentUser.getGender().isEmpty()) {
                tvGender.setText(currentUser.getGender());
            } else {
                tvGender.setText("");
            }

            // 设置年龄
            if (currentUser.getAge() > 0) {
                tvAge.setText(String.valueOf(currentUser.getAge()));
            } else {
                tvAge.setText("");
            }

            // 设置头像
            if (currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
                File avatarFile = new File(currentUser.getAvatar());
                if (avatarFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(currentUser.getAvatar());
                    ivAvatar.setImageBitmap(bitmap);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            // 返回上一页
            finish();
        } else if (id == R.id.rl_avatar) {
            // 头像选择
            showAvatarSelectionPopup();
        } else if (id == R.id.rl_nickname) {
            // 编辑昵称
            showNicknameEditPopup();
        } else if (id == R.id.rl_gender) {
            // 选择性别
            showGenderSelectionPopup();
        } else if (id == R.id.rl_age) {
            // 编辑年龄
            showAgeEditPopup();
        } else if (id == R.id.rl_change_password) {
            // 修改密码
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        }
    }

    // 显示头像选择弹窗
    private void showAvatarSelectionPopup() {
        // 加载弹窗布局
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_avatar_selection, null);
        TextView tvTakePhoto = popupView.findViewById(R.id.tv_take_photo);
        TextView tvPickPhoto = popupView.findViewById(R.id.tv_pick_photo);
        TextView tvCancel = popupView.findViewById(R.id.tv_cancel);

        // 创建弹窗
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);

        // 设置点击事件
        tvTakePhoto.setOnClickListener(v -> {
            dispatchTakePictureIntent();
            popupWindow.dismiss();
        });

        tvPickPhoto.setOnClickListener(v -> {
            pickPhotoFromGallery();
            popupWindow.dismiss();
        });

        tvCancel.setOnClickListener(v -> popupWindow.dismiss());
    }

    // 调用相机拍照
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // 创建临时文件
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error creating image file", ex);
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.calendarapplication.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // 从相册选择照片
    private void pickPhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_PHOTO);
    }

    // 创建临时图片文件
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // 处理相机和相册返回的结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // 相机拍照返回
                setAvatarFromPath(currentPhotoPath);
            } else if (requestCode == REQUEST_PICK_PHOTO) {
                // 相册选择返回
                try {
                    Uri selectedImage = data.getData();
                    // 将Uri转换为真实路径
                    currentPhotoPath = getPathFromUri(selectedImage);
                    setAvatarFromPath(currentPhotoPath);
                } catch (Exception e) {
                    Log.e(TAG, "Error picking photo", e);
                    Toast.makeText(this, "选择照片失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // 根据路径设置头像
    private void setAvatarFromPath(String path) {
        if (path != null && !path.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            ivAvatar.setImageBitmap(bitmap);
            
            // 更新用户头像信息
            if (currentUser != null) {
                currentUser.setAvatar(path);
                int rows = userDAO.updateUser(currentUser);
                if (rows > 0) {
                    Toast.makeText(this, "头像更新成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "头像更新失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // 将Uri转换为真实路径（简化版，实际应用中需要更完善的处理）
    private String getPathFromUri(Uri uri) {
        return currentPhotoPath;
    }

    // 显示昵称编辑弹窗
    private void showNicknameEditPopup() {
        // 加载弹窗布局
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_edit_nickname, null);
        EditText etNickname = popupView.findViewById(R.id.et_nickname);
        TextView tvCancel = popupView.findViewById(R.id.tv_cancel);
        TextView tvConfirm = popupView.findViewById(R.id.tv_confirm);

        // 设置当前昵称
        if (currentUser != null) {
            etNickname.setText(currentUser.getNickname());
            etNickname.setSelection(etNickname.getText().length());
        }

        // 创建弹窗
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);

        // 设置点击事件
        tvCancel.setOnClickListener(v -> popupWindow.dismiss());

        tvConfirm.setOnClickListener(v -> {
            String newNickname = etNickname.getText().toString().trim();
            if (newNickname.isEmpty()) {
                Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (currentUser != null && newNickname.equals(currentUser.getNickname())) {
                Toast.makeText(this, "新昵称不能与原昵称相同", Toast.LENGTH_SHORT).show();
                return;
            }

            // 更新昵称
            if (currentUser != null) {
                currentUser.setNickname(newNickname);
                int rows = userDAO.updateUser(currentUser);
                if (rows > 0) {
                    tvNickname.setText(newNickname);
                    Toast.makeText(this, "昵称更新成功", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                } else {
                    Toast.makeText(this, "昵称更新失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 显示性别选择弹窗
    private void showGenderSelectionPopup() {
        // 加载弹窗布局
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_gender_selection, null);
        TextView tvMale = popupView.findViewById(R.id.tv_male);
        TextView tvFemale = popupView.findViewById(R.id.tv_female);
        TextView tvCancel = popupView.findViewById(R.id.tv_cancel);

        // 创建弹窗
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);

        // 设置点击事件
        tvMale.setOnClickListener(v -> {
            updateGender("男");
            popupWindow.dismiss();
        });

        tvFemale.setOnClickListener(v -> {
            updateGender("女");
            popupWindow.dismiss();
        });

        tvCancel.setOnClickListener(v -> popupWindow.dismiss());
    }

    // 更新性别
    private void updateGender(String gender) {
        if (currentUser != null) {
            currentUser.setGender(gender);
            int rows = userDAO.updateUser(currentUser);
            if (rows > 0) {
                tvGender.setText(gender);
                Toast.makeText(this, "性别更新成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "性别更新失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 显示年龄编辑弹窗
    private void showAgeEditPopup() {
        // 加载弹窗布局
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_edit_age, null);
        EditText etAge = popupView.findViewById(R.id.et_age);
        TextView tvCancel = popupView.findViewById(R.id.tv_cancel);
        TextView tvConfirm = popupView.findViewById(R.id.tv_confirm);

        // 设置当前年龄
        if (currentUser != null && currentUser.getAge() > 0) {
            etAge.setText(String.valueOf(currentUser.getAge()));
            etAge.setSelection(etAge.getText().length());
        }

        // 创建弹窗
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);

        // 设置点击事件
        tvCancel.setOnClickListener(v -> popupWindow.dismiss());

        tvConfirm.setOnClickListener(v -> {
            String ageStr = etAge.getText().toString().trim();
            if (ageStr.isEmpty()) {
                Toast.makeText(this, "年龄不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int age = Integer.parseInt(ageStr);
                if (age < 1 || age > 100) {
                    Toast.makeText(this, "请输入1-100岁的年龄", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 更新年龄
                if (currentUser != null) {
                    currentUser.setAge(age);
                    int rows = userDAO.updateUser(currentUser);
                    if (rows > 0) {
                        tvAge.setText(String.valueOf(age));
                        Toast.makeText(this, "年龄更新成功", Toast.LENGTH_SHORT).show();
                        popupWindow.dismiss();
                    } else {
                        Toast.makeText(this, "年龄更新失败", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "请输入有效的年龄数字", Toast.LENGTH_SHORT).show();
            }
        });
    }


}