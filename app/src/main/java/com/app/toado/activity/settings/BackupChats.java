package com.app.toado.activity.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.app.toado.R;
import com.app.toado.helper.RealmBackupRestore;

/**
 * Created by ghanendra on 01/08/2017.
 */

public class BackupChats extends AppCompatActivity {
    Button btnbackup,btnrest,btnread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backupchats);

        btnbackup=(Button) findViewById(R.id.button_backup);
        btnrest=(Button) findViewById(R.id.button_restore);

        final RealmBackupRestore rel1 = new RealmBackupRestore(BackupChats.this);
        btnrest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("restore clicked");
                rel1.restore();
            }
        });

        btnbackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("backup clicked");
                 rel1.backup();
            }
        });

    }
}
