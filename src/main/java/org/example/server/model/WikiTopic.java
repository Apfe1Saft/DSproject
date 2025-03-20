package org.example.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WikiTopic {
    private String title;
    private String description;
    private String link;

    @Override
    public String toString() {
        return title + "\nLink: " + link + "\nDescription: " + description;
    }
}
