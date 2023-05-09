package com.kausar.roomwordssample;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kausar.roomwordssample.room.Word;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int NEW_WORD_ACTIVITY_REQUEST_CODE = 1;
    public static final int UPDATE_WORD_ACTIVITY_REQUEST_CODE = 2;

    public static final String EXTRA_DATA_UPDATE_WORD = "extra_word_to_be_updated";
    public static final String EXTRA_DATA_ID = "extra_data_id";

    private WordViewModel mWordViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWordViewModel = new ViewModelProvider(this).get(WordViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final WordListAdapter adapter = new WordListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mWordViewModel.getAllWords().observe(this, new Observer<List<Word>>() {
            @Override
            public void onChanged(List<Word> words) {
                adapter.setWords(words);
            }
        });

        findViewById(R.id.fav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewWordActivity.class);
                startActivityForResult(intent, NEW_WORD_ACTIVITY_REQUEST_CODE);
//                launchActivity.launch(intent);
            }
        });

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                int position = viewHolder.getAdapterPosition();
                Word myWord = adapter.getWordAtPosition(position);
                Toast.makeText(MainActivity.this, "Deleting " +
                        myWord.getWord(), Toast.LENGTH_LONG).show();

                // Delete the word
                mWordViewModel.deleteWord(myWord);

            }
        });

        helper.attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new WordListAdapter.ClickListener()  {

            @Override
            public void onItemClick(View v, int position) {
                Word word = adapter.getWordAtPosition(position);
                launchUpdateWordActivity(word);
            }
        });


    }

    private void launchUpdateWordActivity(Word word) {
        Intent intent = new Intent(this, NewWordActivity.class);
        intent.putExtra(EXTRA_DATA_UPDATE_WORD, word.getWord());
        intent.putExtra(EXTRA_DATA_ID, word.getId());
        startActivityForResult(intent, UPDATE_WORD_ACTIVITY_REQUEST_CODE);
    }

//    private ActivityResultLauncher<Intent> launchActivity = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//
//                    if (result.getResultCode() == RESULT_OK) {
//                        Word word = new Word(result.getData().getStringExtra(NewWordActivity.EXTRA_REPLY));
//                        mWordViewModel.insert(word);
//                    } else {
//                        Toast.makeText(
//                                getApplicationContext(),
//                                R.string.empty_not_saved,
//                                Toast.LENGTH_LONG).show();
//                    }
//
//                }
//            }
//    );

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear_data) {
            Toast.makeText(this, getString(R.string.clear_data_toast_text), Toast.LENGTH_SHORT).show();

            mWordViewModel.deleteAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

        @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == NEW_WORD_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
                Word word = new Word(data.getStringExtra(NewWordActivity.EXTRA_REPLY));
                // Save the data.
                mWordViewModel.insert(word);
            } else if (requestCode == UPDATE_WORD_ACTIVITY_REQUEST_CODE
                    && resultCode == RESULT_OK) {
                String word_data = data.getStringExtra(NewWordActivity.EXTRA_REPLY);
                int id = data.getIntExtra(NewWordActivity.EXTRA_REPLY_ID, -1);

                if (id != -1) {
                    mWordViewModel.update(new Word(id, word_data));
                } else {
                    Toast.makeText(this, R.string.unable_to_update,
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(
                        this, R.string.empty_not_saved, Toast.LENGTH_LONG).show();
            }

    }
}