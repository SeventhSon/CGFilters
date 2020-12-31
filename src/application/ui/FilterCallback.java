package application.ui;

import application.services.FilterTask;

public interface FilterCallback {
    FilterTask filter(float strength);
}
