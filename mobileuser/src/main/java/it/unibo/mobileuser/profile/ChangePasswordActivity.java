package it.unibo.mobileuser.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import it.unibo.mobileuser.R;
import it.unibo.mobileuser.ToolbarActivity;

/**
 * A class that allows to show the graphical interface to change the password of account and save the changes on server.
 */
public class ChangePasswordActivity extends ToolbarActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        setToolbar(true);

        final Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                //TODO: Add method to send to server the changes
            }
        });
    }
}
