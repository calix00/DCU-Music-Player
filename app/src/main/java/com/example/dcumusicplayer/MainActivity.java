package com.example.dcumusicplayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    ImageView[] imgMusic = new ImageView[9];
    TextView[] txtMusicTitle = new TextView[9];
    TextView[] txtSingerName = new TextView[9];
    Button[] btnPlayMusic = new Button[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // XML 레이아웃에서 뷰 참조 가져오기
        for (int i = 0; i < 9; i++) {
            int imgId = getResources().getIdentifier("imgMusic" + (i + 1), "id", getPackageName());
            imgMusic[i] = findViewById(imgId);

            int txtTitleId = getResources().getIdentifier("txt_music_title" + (i + 1), "id", getPackageName());
            txtMusicTitle[i] = findViewById(txtTitleId);

            int txtSingerId = getResources().getIdentifier("txt_singer_name" + (i + 1), "id", getPackageName());
            txtSingerName[i] = findViewById(txtSingerId);

            int btnId = getResources().getIdentifier("isMusic_click" + (i + 1), "id", getPackageName());
            btnPlayMusic[i] = findViewById(btnId);

            // 각 버튼에 클릭 리스너 설정
            setButtonClickListener(btnPlayMusic[i]);

            // 노래 제목과 가수 이름 설정
            txtMusicTitle[i].setText("노래 제목" + (i + 1));
            txtSingerName[i].setText("가수 이름" + (i + 1));

            // 버튼에 텍스트 설정
            btnPlayMusic[i].setText("3:20");
        }
    }

    // 각 버튼에 클릭 리스너 설정하는 메서드
    private void setButtonClickListener(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage("음악을 재생합니다.");
            }
        });
    }

    // 간단한 메시지를 표시하는 메서드
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}