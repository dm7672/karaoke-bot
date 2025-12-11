package model.domain.parcer;

import model.domain.entities.Video;

public interface IUrlParser {
    boolean isValid(String url);
    Video parse(String url);
}
