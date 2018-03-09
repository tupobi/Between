package www.tupobi.top.between.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.widget.EditText;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import www.tupobi.top.between.R;
import www.tupobi.top.between.utils.ToastUtil;

public class AtyLogin extends AppCompatActivity {


    @BindView(R.id.et_username)
    EditText mEtUsername;
    @BindView(R.id.et_password)
    EditText mEtPassword;
    @BindView(R.id.btn_login)
    AppCompatButton mBtnLogin;

    public static void actionStart(Context context) {
        context.startActivity(new Intent(context, AtyLogin.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_login)
    public void onViewClicked() {
//        AtyMain.actionStart(AtyLogin.this);
        final String username = mEtUsername.getText().toString();
        final String password = mEtPassword.getText().toString();
        if (username.isEmpty() || password.isEmpty()) {
            ToastUtil.showShort(AtyLogin.this, "账号密码不能为空！");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                EMClient.getInstance().login(username, password, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showShort(AtyLogin.this, "登录成功！");
                                Logger.e("登陆过：hxid == " + EMClient.getInstance().getCurrentUser());
                                String hxid = EMClient.getInstance().getCurrentUser();
                                if ("cjm520".equals(hxid)) {
                                    AtyMain.actionStart(AtyLogin.this, "lzj7800623");
                                    finish();
                                } else if ("lzj7800623".equals(hxid)) {
                                    AtyMain.actionStart(AtyLogin.this, "cjm520");
                                    finish();
                                } else {
                                    ToastUtil.showShort(AtyLogin.this, "非法用户..");
                                    finish();
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(int i, String s) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showShort(AtyLogin.this, "账号密码错误！");
                            }
                        });
                    }

                    @Override
                    public void onProgress(int i, String s) {

                    }
                });
            }
        }).start();
    }
}
