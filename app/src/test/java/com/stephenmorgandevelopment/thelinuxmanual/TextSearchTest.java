package com.stephenmorgandevelopment.thelinuxmanual;

import android.util.Log;

import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlAdapter;
import com.stephenmorgandevelopment.thelinuxmanual.models.Command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

//@RunWith(AndroidJUnit4.class)
public class TextSearchTest {


//    @Test
//    public void command_searchDataForTextMatch_ReturnsCorrectCount() {
//        int expectedCount = 73;
//        String searchQuery = "transfer";
//
//        TextSearchResult results =
//                getTestCommand().searchDataForTextMatch(searchQuery);
//
//        Log.i("TextSearchText", "results.getCount() : " + results.getCount());
//        Assert.assertEquals(expectedCount, results.getCount());
//    }




    private Command getTestCommand() {
        StringBuilder manPageHtml = new StringBuilder();

        try {
            File testFile = new File("src/main/assets/curlManPage.html");

            InputStreamReader reader = new InputStreamReader(new FileInputStream(testFile));

//            InputStreamReader reader = new InputStreamReader(
//                    ApplicationProvider.getApplicationContext()
//                            .getAssets().open("curlManPage.json")
//            );

            BufferedReader bufferedReader = new BufferedReader(reader, 4096);

            String line;
            while((line = bufferedReader.readLine()) != null) {
                manPageHtml.append(line);
            }

            bufferedReader.close();
//            reader.close();
        } catch (IOException ioe) {
            Log.d("TextSearchText", "getTestCommand io error: " + ioe.getMessage());
        }

        return new Command(1, UbuntuHtmlAdapter.crawlForCommandInfo(manPageHtml.toString()));
    }
}
