var testJson = {
    "name": "testOne",
    "description": "This is Demo Json",
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
            "status": "failure",
            "reportElements": [
                {
                    "timestamp": "11:49:23",
                    "title": "Clickin on some element"

                },
                {
                    "timestamp": "11:49:24",
                    "title": "Clickin on some element",
                    "message": "This is some message"

                },
                {
                    "timestamp": "11:49:25",
                    "title": "This is a screenshot",
                    "file": "resources/Capture323234.PNG",
                    "type": "IMAGE"
                }
            ]

        }
    ]


}
