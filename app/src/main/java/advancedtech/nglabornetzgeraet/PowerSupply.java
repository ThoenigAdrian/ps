package advancedtech.nglabornetzgeraet;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PowerSupply extends AppCompatActivity {


    ArrayList<Button> voltageCurrentButtons = new ArrayList<>();
    HighLevelCommunicationInterface hlc = HighLevelCommunicationInterface.getInstance();
    Beacon passedPowerSuppyLnfo;
    String response;

    private class asyncSendToPowerSupply extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... params) {
            return hlc.sendMessageToPowerSupply(params[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            onMessageSent(s);
        }
    }

    private class connectToPowerSupply extends AsyncTask<Void,Void,Void> {

        private String error_string="";

        @Override
        protected Void doInBackground(Void[] params) {
            error_string = hlc.connectToPowerSupply(passedPowerSuppyLnfo);
            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if(hlc.isPowerSupplyConnected())
                onConnectionSuccessfull(error_string);
            else
                onConnectionFailed(error_string);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_supply_connecting);
        Button increase_current_channel_1 = (Button) findViewById(R.id.increase_current_channel_1);
        Button increase_voltage_channel_1 = (Button) findViewById(R.id.increase_voltage_channel_1);
        Button increase_voltage_channel_2 = (Button) findViewById(R.id.increase_voltage_channel_2);
        Button increase_current_channel_2 = (Button) findViewById(R.id.increase_current_channel_2);
        Button decrease_current_channel_1 = (Button) findViewById(R.id.decrease_current_channel_1);
        Button decrease_current_channel_2 = (Button) findViewById(R.id.decrease_current_channel_2);
        Button decrease_voltage_channel_1 = (Button) findViewById(R.id.decrease_voltage_channel_1);
        Button decrease_voltage_channel_2 = (Button) findViewById(R.id.decrease_voltage_channel_2);
        voltageCurrentButtons.add(increase_current_channel_1);
        voltageCurrentButtons.add(increase_voltage_channel_1);
        voltageCurrentButtons.add(increase_voltage_channel_2);
        voltageCurrentButtons.add(increase_current_channel_2);
        voltageCurrentButtons.add(decrease_current_channel_1);
        voltageCurrentButtons.add(decrease_current_channel_2);
        voltageCurrentButtons.add(decrease_voltage_channel_1);
        voltageCurrentButtons.add(decrease_voltage_channel_2);

        passedPowerSuppyLnfo = new Beacon((getIntent().getExtras().getString("beaconInfo")));
        connectToPowerSupply asyncConnectTask = new connectToPowerSupply();
        asyncConnectTask.execute();

    }

    public void onMessageSent(String response){

    }

    public void onConnectionFailed(String reason){
        Toast.makeText(getApplicationContext(), reason, Toast.LENGTH_LONG).show();
        finish();
    }

    public void onConnectionSuccessfull(String connectionAcceptResponse){

        for(Button controlButton : voltageCurrentButtons){
            controlButton.setClickable(true);
        }

        Button connectionStatus = (Button) findViewById(R.id.connectionStatus);
        connectionStatus.setText(R.string.connected_string);
        connectionStatus.setBackgroundColor(Color.parseColor("#0ece1b"));
        EditText liveDataChannel1 = (EditText) findViewById(R.id.live_data_content_channel1);
        EditText liveDataChannel2 = (EditText) findViewById(R.id.live_data_content_channel2);
        liveDataChannel1.setVisibility(View.VISIBLE);
        liveDataChannel2.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.voltage_channel_1)).setTextColor(Color.BLACK);
        ((TextView) findViewById(R.id.voltage_channel_2)).setTextColor(Color.BLACK);
        ((TextView) findViewById(R.id.current_channel_1)).setTextColor(Color.BLACK);
        ((TextView) findViewById(R.id.current_channel_2)).setTextColor(Color.BLACK);
        setAllData(connectionAcceptResponse);

    }



    private void setAll(Channel channel1, Channel channel2){
        asyncSendToPowerSupply a = new asyncSendToPowerSupply();
        String command = "set_all";
        JSONObject message = new JSONObject();
        try{
            message.put("command", command);

            JSONObject channel1_data = new JSONObject();
            channel1_data.put("voltage", channel1.getVoltage());
            channel1_data.put("voltage", channel1.geCurrent());

            JSONObject channel2_data = new JSONObject();
            channel2_data.put("voltage", channel2.getVoltage());
            channel2_data.put("voltage", channel2.geCurrent());

            message.put("channel1", channel1_data);
            message.put("channel2", channel2_data);
        } catch (JSONException e){

        }
        a.doInBackground(message.toString());

    }

    private void setVoltage(Integer channelNr, Float voltage){
        asyncSendToPowerSupply a = new asyncSendToPowerSupply();
        String command = "set_voltage";
        JSONObject message = new JSONObject();
        try{
            message.put("command", command);
            message.put("channel", channelNr.toString());
            message.put("value", String.format(java.util.Locale.US, "%.1f", voltage));
        } catch (JSONException e) {

        }
        a.doInBackground(message.toString());
    }

    private void setCurrent(Integer channelNr, Float current){
        asyncSendToPowerSupply a = new asyncSendToPowerSupply();
        String command = "set_current";
        JSONObject message = new JSONObject();
        try{
            message.put("command", command);
            message.put("channel", channelNr.toString());
            message.put("value", String.format(java.util.Locale.US, "%.1f", current));
        } catch (JSONException e) {

        }
        a.doInBackground(message.toString());
    }

    private class Channel{
        private Float voltage;
        private Float current;

        public Channel(Float voltage, Float current){
            this.voltage = voltage;
            this.current = current;
        }

        public String getVoltage() {
            return String.format(java.util.Locale.US, "%.1f", voltage);
        }

        public String geCurrent() {
            return String.format(java.util.Locale.US, "%.1f", current);
        }
    }

    public  void setAllData(String resp)
    {
        try {
            JSONObject reader = new JSONObject(resp);
            JSONObject data = reader.getJSONObject("data");
            EditText liveDataChannel1 = (EditText) findViewById(R.id.live_data_content_channel1);
            EditText liveDataChannel2 = (EditText) findViewById(R.id.live_data_content_channel2);

            String current = data.getString("live_current_channel_1");
            String voltage = data.getString("live_voltage_channel_1");
            String power = ((Float) (Float.parseFloat(voltage) * Float.parseFloat(current))).toString();
            liveDataChannel1.setText("Leistung: " + power + "W\n" + "Spannung: " + voltage + "V\n" +
                    "Strom: " + current + "A\n");

            current = data.getString("live_current_channel_2");
            voltage = data.getString("live_voltage_channel_2");
            power = ((Float) (Float.parseFloat(voltage) * Float.parseFloat(current))).toString();
            liveDataChannel2.setText("Leistung: " + power + "W\n" + "Spannung: " + voltage + "V\n" +
                    "Strom: " + current + "A\n");
        } catch (JSONException e) {
            Toast.makeText(this, "Error during setAllData" + e.toString(), Toast.LENGTH_LONG).show();
        } catch (NullPointerException e){
            Toast.makeText(this, "Error during setAllData" + e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
