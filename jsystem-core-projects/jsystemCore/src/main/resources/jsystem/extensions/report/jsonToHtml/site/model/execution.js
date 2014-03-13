var execution = {
    "machines": [
        {
            "name": "127.0.0.1",
            "children": [
                {
                    "type": "scenario",
                    "name": "myScenario",
                    "status": "failure",
                    "children": [
                        {
                            "type": "test",
                            "index": "0",
                            "name": "testOne",
                            "status": "success",
                            "duration": "1750",
                            "timestamp": "11:49:23"
                        },
                        {
                            "type": "test",
                            "index": "1",
                            "name": "testTwo",
                            "status": "error",
                            "duration": "1750",
                            "timestamp": "11:50:23"
                        },
                        {
                            "type": "test",
                            "index": "2",
                            "name": "testThree",
                            "status": "failure",
                            "duration": "173",
                            "timestamp": "11:51:23"

                        },
                        {
                            "type": "scenario",
                            "name": "innerScenario",
                            "status": "success",
                            "children": [
                                {
                                    "type": "test",
                                    "index": "3",
                                    "name": "testFour",
                                    "status": "success",
                                    "duration": "174",
                                    "timestamp": "11:51:23"

                                }, {
                                    "type": "test",
                                    "index": "4",
                                    "name": "testFive",
                                    "status": "success",
                                    "duration": "176",
                                    "timestamp": "12:15:23"
                                }

                            ]

                        }, {
                            "type": "test",
                            "index": "5",
                            "name": "testSix",
                            "status": "success",
                            "duration": "173",
                            "timestamp": "12:20:23"
                        },
                        {
                            "type": "scenario",
                            "name": "Scenario with warning",
                            "status": "warning",
                            "children": [
                                {
                                    "type": "test",
                                    "index": "6",
                                    "name": "testSeven",
                                    "status": "success",
                                    "duration": "174",
                                    "timestamp": "11:51:23"

                                }, {
                                    "type": "test",
                                    "index": "7",
                                    "name": "testEight",
                                    "status": "warning",
                                    "duration": "176",
                                    "timestamp": "12:15:23"
                                }, {
                                    "type": "scenario",
                                    "name": "Scenario with warning",
                                    "status": "warning",
                                    "children": [
                                        {
                                            "type": "test",
                                            "index": "8",
                                            "name": "testSeven",
                                            "status": "success",
                                            "duration": "174",
                                            "timestamp": "11:51:23"

                                        }, {
                                            "type": "test",
                                            "index": "9",
                                            "name": "testEight",
                                            "status": "success",
                                            "duration": "176",
                                            "timestamp": "12:15:23"
                                        }, {
                                            "type": "scenario",
                                            "name": "Scenario with warning",
                                            "status": "warning",
                                            "children": [
                                                {
                                                    "type": "test",
                                                    "index": "10",
                                                    "name": "testSeven",
                                                    "status": "success",
                                                    "duration": "174",
                                                    "timestamp": "11:51:23"

                                                }, {
                                                    "type": "test",
                                                    "index": "11",
                                                    "name": "testEight",
                                                    "status": "failure",
                                                    "duration": "176",
                                                    "timestamp": "12:15:23"
                                                }, {
                                                    "type": "scenario",
                                                    "name": "Scenario with warning",
                                                    "status": "warning",
                                                    "children": [
                                                        {
                                                            "type": "test",
                                                            "index": "12",
                                                            "name": "testSeven",
                                                            "status": "failure",
                                                            "duration": "1734",
                                                            "timestamp": "11:51:23"

                                                        }, {
                                                            "type": "test",
                                                            "index": "13",
                                                            "name": "testEight",
                                                            "status": "warning",
                                                            "duration": "176",
                                                            "timestamp": "12:15:23"
                                                        }, {
                                                            "type": "scenario",
                                                            "name": "Scenario with warning",
                                                            "status": "warning",
                                                            "children": [
                                                                {
                                                                    "type": "test",
                                                                    "index": "14",
                                                                    "name": "testSeven",
                                                                    "status": "success",
                                                                    "duration": "174",
                                                                    "timestamp": "11:51:23"

                                                                }, {
                                                                    "type": "test",
                                                                    "index": "15",
                                                                    "name": "testEight",
                                                                    "status": "success",
                                                                    "duration": "176",
                                                                    "timestamp": "12:15:23"
                                                                }

                                                            ]

                                                        }

                                                    ]

                                                }

                                            ]

                                        }

                                    ]

                                }

                            ]

                        }
                    ]

                }

            ]
        },
        {
            "name": "192.128.10.145",
            "children": [
                {
                    "type": "scenario",
                    "name": "anotherScenario",
                    "status": "failure",
                    "children": [
                        {
                            "type": "test",
                            "index": "0",
                            "name": "testOne",
                            "status": "error",
                            "duration": "1750",
                            "timestamp": "11:49:23"
                        },
                        {
                            "type": "test",
                            "index": "1",
                            "name": "testTwo",
                            "status": "warning",
                            "duration": "1750",
                            "timestamp": "11:50:23"
                        },
                        {
                            "type": "test",
                            "index": "2",
                            "name": "testThree",
                            "status": "failure",
                            "duration": "173",
                            "timestamp": "11:51:23"

                        },
                        {
                            "type": "test",
                            "index": "3",
                            "name": "testSix",
                            "status": "success",
                            "duration": "173",
                            "timestamp": "12:20:23"
                        }
                       
                    ]

                }

            ]
        }
    ]

};
