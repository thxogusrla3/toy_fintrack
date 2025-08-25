import React, {useEffect, useState} from "react";
import axios from "axios";

function Sample() {
    const [data, setData] = useState("");
    useEffect(() => {
        axios.get("/home")
            .then(res => setData(res.data))
            .catch(error => console.log(error))
    }, []);
    return (
        <div className="Sample">
            sample: {data}
        </div>
    )
}
export default Sample;
