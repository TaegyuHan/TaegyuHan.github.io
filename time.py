from datetime import datetime

template = "last_modified_at: 2023-07-27T14:14:00"
date_str = template.split(": ")[-1]
last_modified_time = datetime.strptime(date_str, "%Y-%m-%dT%H:%M:%S")

current_time = datetime.now()
time_difference = current_time - last_modified_time

print("last_modified_at:", current_time)
