var testJson = {
    "name": "testOne",
    "description": "This is the first test",
    "timestamp": "22/01/2014",
    "duration": "343445",
    "parameters": [
        {"param0": "val0"}, {"param1": "val1"}, {"param2": "val2"}
    ],
    "properties": [
        {"prop0": "val0"}, {"prop1": "val1"}
    ],
    "steps": [
        {
            "timestamp": "11:49:22",
            "description": "This is the first step",
            "status": "success",
            "reportElements": [
                {
                    "timestamp": "11:49:23",
                    "status":"success",
                    "title": "Clickin on some element"
                },
                {
                    "timestamp": "11:49:24",
                    "status":"success",
                    "title": "Open some element - MESSAGE",
                    "message": "This is some message"

                },
                {
                    "timestamp": "11:49:25",
                    "status":"success",
                    "title": "This is a screenshot - IMAGE",
                    "file": "resources/Capture323234.PNG",
                    "type": "IMAGE"
                }
            ]
        },
        {
            "timestamp": "11:50:22",
            "description": "This is the second step",
            "status": "failure",
            "reportElements": [
                {
                    "timestamp": "11:50:23",
                    "status":"success",
                    "title": "Clickin on some button"

                },
                {
                    "timestamp": "11:50:24",
                    "status":"warning",
                    "title": "Select some element from list - MESSAGE",
                    "message": "This is some message"

                },
                {
                    "timestamp": "11:50:25",
                    "status":"failure",
                    "title": "This is a screenshot of the element - VIDEO",
                    "file": "resources/Capture323234.PNG",
                    "type": "VIDEO"
                }
            ]
        },
        {
            "timestamp": "11:51:22",
            "description": "This is the Third step",
            "status": "warning",
            "reportElements": [
                {
                    "timestamp": "11:51:23",
                    "status":"failure",
                    "title": "Clickin on some element"

                },
                {
                    "timestamp": "11:51:24",
                    "status":"warning",
                    "title": "Clickin on some element - MESSAGE",
                    "message": "This is some message"

                },
                {
                    "timestamp": "11:51:25",
                    "status":"success",
                    "title": "This is a screenshot - LOG",
                    "file": "resources/Capture323234.PNG",
                    "type": "LOG"
                }
            ]
        }
    ]
}
