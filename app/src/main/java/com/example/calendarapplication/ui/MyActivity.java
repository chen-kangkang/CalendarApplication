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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.calendarapplication.R;
import com.example.calendarapplication.db.UserDAO;
import com.example.calendarapplication.model.User;

public class MyActivity extends AppCompatActivity implements View.OnClickListener {

    // UI组件
    private ImageView ivAvatar;
    private TextView tvNickname;
    private Button btnLoginOrLogout;
    private RelativeLayout rlBasicInfo;
    private RelativeLayout rlPushNotification;
    private RelativeLayout rlMySchedules;

    // 底部导航栏组件
    private LinearLayout llNavSchedule;
    private LinearLayout llNavView;
    private LinearLayout llNavMine;
    private ImageView ivNavSchedule;
    private ImageView ivNavView;
    private ImageView ivNavMine;

    // 数据库操作对象
    private UserDAO userDAO;

    // 头像相关常量
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_PHOTO = 2;
    
    // 头像文件路径
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        // 初始化UI组件
        initViews();

        // 初始化数据库操作对象
        userDAO = new UserDAO(this);

        // 检查登录状态并更新UI
        checkLoginStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 当页面恢复时，重新检查登录状态并刷新用户信息
        checkLoginStatus();
    }

    private void initViews() {
        // 用户信息相关组件
        ivAvatar = findViewById(R.id.iv_avatar);
        tvNickname = findViewById(R.id.tv_nickname);
        btnLoginOrLogout = findViewById(R.id.btn_login_or_logout);

        // 功能选项组件
        rlBasicInfo = findViewById(R.id.rl_basic_info);
        rlPushNotification = findViewById(R.id.rl_push_notification);
        rlMySchedules = findViewById(R.id.rl_my_schedules);

        // 底部导航栏组件
        llNavSchedule = findViewById(R.id.ll_nav_schedule);
        llNavView = findViewById(R.id.ll_nav_view);
        llNavMine = findViewById(R.id.ll_nav_mine);
        ivNavSchedule = findViewById(R.id.iv_nav_schedule);
        ivNavView = findViewById(R.id.iv_nav_view);
        ivNavMine = findViewById(R.id.iv_nav_mine);

        // 设置点击事件监听
        btnLoginOrLogout.setOnClickListener(this);
        rlBasicInfo.setOnClickListener(this);
        rlPushNotification.setOnClickListener(this);
        rlMySchedules.setOnClickListener(this);
        llNavSchedule.setOnClickListener(this);
        llNavView.setOnClickListener(this);
        llNavMine.setOnClickListener(this);
        ivAvatar.setOnClickListener(this);
        tvNickname.setOnClickListener(this);

        // 设置当前页面底部导航为选中状态
        updateNavSelection(2);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_login_or_logout) {
            // 登录/退出登录按钮点击事件
            handleLoginOrLogout();
        } else if (id == R.id.rl_basic_info || id == R.id.rl_push_notification || id == R.id.rl_my_schedules || id == R.id.iv_avatar) {
            // 检查是否已登录
            if (!isUserLoggedIn()) {
                // 未登录，跳转到登录页面
                gotoLoginActivity();
            } else {
                // 已登录，根据点击的选项执行相应操作
                if (id == R.id.rl_basic_info) {
                    // 跳转到基本信息页面
                    Intent intent = new Intent(this, BasicInfoActivity.class);
                    startActivity(intent);
                } else if (id == R.id.rl_push_notification) {
                    // 跳转到推送提醒页面
                    Intent intent = new Intent(this, PushNotificationActivity.class);
                    startActivity(intent);
                } else if (id == R.id.rl_my_schedules) {
                    // 跳转到我的日程页面
                    Intent intent = new Intent(this, MyScheduleActivity.class);
                    startActivity(intent);
                } else if (id == R.id.iv_avatar) {
                    // 显示头像选择弹窗
                    showAvatarSelectionPopup();
                } else if (id == R.id.tv_nickname) {
                    // 显示昵称编辑弹窗
                    showNicknameEditPopup();
                }
            }
        } else if (id == R.id.ll_nav_schedule) {
            // 跳转到日程页面
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            updateNavSelection(0);
        } else if (id == R.id.ll_nav_view) {
            // 跳转到视图页面
            Intent intent = new Intent(this, ViewActivity.class);
            startActivity(intent);
            updateNavSelection(1);
        } else if (id == R.id.ll_nav_mine) {
            // 已经在我的页面，无需跳转
            updateNavSelection(2);
        }
    }

    /**
     * 检查登录状态并更新UI
     */
    private void checkLoginStatus() {
        if (isUserLoggedIn()) {
            // 已登录，显示用户信息
            showUserInfo();
        } else {
            // 未登录，显示默认UI
            showDefaultUI();
        }
    }

    /**
     * 检查用户是否已登录
     */
    private boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String phone = sharedPreferences.getString("phone", null);
        long loginTime = sharedPreferences.getLong("login_time", 0);

        if (phone != null && loginTime != 0) {
            // 计算登录时间和当前时间的差值（毫秒）
            long currentTime = System.currentTimeMillis();
            long diffTime = currentTime - loginTime;
            // 3天的毫秒数：3 * 24 * 60 * 60 * 1000 = 259200000
            return diffTime < 3 * 24 * 60 * 60 * 1000;
        }
        return false;
    }

    /**
     * 显示已登录用户信息
     */
    private void showUserInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String phone = sharedPreferences.getString("phone", null);

        if (phone != null) {
            // 查询用户信息
            User user = userDAO.queryUserByPhone(phone);
            if (user != null) {
                // 显示用户名
                tvNickname.setVisibility(View.VISIBLE);
                tvNickname.setText(user.getNickname());

                // 显示用户头像
                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    // 如果用户有自定义头像，加载头像
                    ivAvatar.setImageURI(Uri.parse(user.getAvatar()));
                } else {
                    // 使用默认头像
                    ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                }

                // 更新按钮文字为"退出登录"
                btnLoginOrLogout.setText("退出登录");
                btnLoginOrLogout.setBackgroundResource(R.drawable.button_gray_bg);
            }
        }
    }

    /**
     * 显示未登录默认UI
     */
    private void showDefaultUI() {
        // 隐藏用户名
        tvNickname.setVisibility(View.GONE);

        // 使用默认头像
        ivAvatar.setImageResource(R.drawable.ic_default_avatar);

        // 更新按钮文字为"立即登录"
        btnLoginOrLogout.setText("立即登录");
        btnLoginOrLogout.setBackgroundResource(R.drawable.button_gray_bg);
    }

    /**
     * 处理登录/退出登录按钮点击事件
     */
    private void handleLoginOrLogout() {
        if (isUserLoggedIn()) {
            // 已登录，执行退出登录操作
            logout();
        } else {
            // 未登录，跳转到登录页面
            gotoLoginActivity();
        }
    }

    /**
     * 退出登录
     */
    private void logout() {
        // 清除SharedPreferences中的登录信息
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("phone");
        editor.remove("login_time");
        editor.remove("user_id");
        editor.apply();

        // 显示退出登录成功提示
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();

        // 跳转到登录页面
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // 显示昵称编辑弹窗
    private void showNicknameEditPopup() {
        // 获取当前登录用户的手机号
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String phone = sharedPreferences.getString("phone", null);
        if (phone == null) {
            return;
        }

        // 查询当前用户信息
        User user = userDAO.queryUserByPhone(phone);
        if (user == null) {
            return;
        }

        // 加载弹窗布局
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_edit_nickname, null);
        EditText etNickname = popupView.findViewById(R.id.et_nickname);
        TextView tvCancel = popupView.findViewById(R.id.tv_cancel);
        TextView tvConfirm = popupView.findViewById(R.id.tv_confirm);

        // 设置当前昵称
        etNickname.setText(user.getNickname());
        etNickname.setSelection(etNickname.getText().length());

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
            
            if (newNickname.equals(user.getNickname())) {
                Toast.makeText(this, "新昵称不能与原昵称相同", Toast.LENGTH_SHORT).show();
                return;
            }

            // 更新昵称
            user.setNickname(newNickname);
            int rows = userDAO.updateUser(user);
            if (rows > 0) {
                tvNickname.setText(newNickname);
                Toast.makeText(this, "昵称更新成功", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            } else {
                Toast.makeText(this, "昵称更新失败", Toast.LENGTH_SHORT).show();
            }
        });
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
                Toast.makeText(this, "创建照片文件失败", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "选择照片失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // 根据路径设置头像
    private void setAvatarFromPath(String path) {
        if (path != null && !path.isEmpty()) {
            // 加载头像
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            ivAvatar.setImageBitmap(bitmap);
            
            // 更新数据库中的头像信息
            SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
            String phone = sharedPreferences.getString("phone", null);
            if (phone != null) {
                User user = userDAO.queryUserByPhone(phone);
                if (user != null) {
                    user.setAvatar(path);
                    int rows = userDAO.updateUser(user);
                    if (rows > 0) {
                        Toast.makeText(this, "头像更新成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "头像更新失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    // 将Uri转换为真实路径
    private String getPathFromUri(Uri uri) {
        // 简化实现，实际应用中需要更完善的处理
        return currentPhotoPath;
    }

    /**
     * 跳转到登录页面
     */
    private void gotoLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * 更新底部导航栏选中状态
     *
     * @param position 选中的位置：0-日程，1-视图，2-我的
     */
    private void updateNavSelection(int position) {
        // 重置所有图标为未选中状态
        ivNavSchedule.setImageResource(R.drawable.ic_nav_schedule_unselected);
        ivNavView.setImageResource(R.drawable.ic_nav_view_unselected);
        ivNavMine.setImageResource(R.drawable.ic_nav_mine_unselected);

        // 设置当前选中项的图标为选中状态
        switch (position) {
            case 0:
                ivNavSchedule.setImageResource(R.drawable.ic_nav_schedule_selected);
                break;
            case 1:
                ivNavView.setImageResource(R.drawable.ic_nav_view_selected);
                break;
            case 2:
                ivNavMine.setImageResource(R.drawable.ic_nav_mine_selected);
                break;
        }
    }
}