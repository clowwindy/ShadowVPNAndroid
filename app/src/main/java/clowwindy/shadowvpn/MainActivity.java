package clowwindy.shadowvpn;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onStartButtonClick(View view) {
        Intent intent = VpnService.prepare(this);

        if (intent != null)
            startActivityForResult(intent, 0);
        else
            onActivityResult(0, RESULT_OK, null);
    }


    @Override
    protected void onActivityResult(int request, int result, Intent data)
    {
        if (result == RESULT_OK)
        {
            Intent intent = new Intent(this, MyVPNService.class);
            startService(intent);
        }
    }

    public void onStopButtonClick(View view) {

    }
}
