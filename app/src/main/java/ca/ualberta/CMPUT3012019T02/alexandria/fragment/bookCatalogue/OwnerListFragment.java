package ca.ualberta.CMPUT3012019T02.alexandria.fragment.bookCatalogue;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ca.ualberta.CMPUT3012019T02.alexandria.controller.BookController;
import java9.util.concurrent.CompletableFuture;

import ca.ualberta.CMPUT3012019T02.alexandria.R;
import ca.ualberta.CMPUT3012019T02.alexandria.adapter.OwnerRecyclerViewAdapter;
import ca.ualberta.CMPUT3012019T02.alexandria.model.OwnerListItem;

public class OwnerListFragment extends Fragment {

    private List<OwnerListItem> owners = new ArrayList<>();
    private String author;
    private String isbn;
    private String title;
    private ArrayList<String> availableOwners;

    private static BookController bookController = BookController.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_book_catalogue_owners, null);

        RecyclerView mRecyclerView = rootView.findViewById(R.id.book_catalogue_recycler);
        OwnerRecyclerViewAdapter userAdapter = new OwnerRecyclerViewAdapter(getContext(), owners);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(userAdapter);

        return rootView;
    }

    public void dataGrab(String title, String author, String isbn, ArrayList<String> availableOwners){
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.availableOwners = availableOwners;

        CompletableFuture<ArrayList<OwnerListItem>> aoFuture = bookController.getAvailableOwners(isbn, availableOwners, title, author);

        aoFuture.handleAsync(
                (owners, error) -> {
                    if (error == null) {
                       owners.addAll(owners);
                    } else {
                        error.printStackTrace();
                    }
                    return null;
                }
        );
    }
}
