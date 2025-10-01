import React, { useEffect, useState } from 'react';
import api from '../services/api';

function TransactionList() {
    const [transactions, setTransactions] = useState([]);

    useEffect(() => {
        api.get("/transactions/?page=0&size=20&sort=id,asc")
        .then(response => {
            setTransactions(response.data.content || []);
        })
    }, []);

    return (
        <div style={{ maxWidth: '800px', margin: '20px auto' }}>
            <h2>거래내역</h2>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                    <tr>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>ID</th>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>날짜</th>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>유형</th>  
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>카테고리</th>  
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>금액</th>  
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>메모</th>
                    </tr>
                </thead>
                <tbody>
                    {transactions.map(tx => (
                        <tr key={tx.id}>
                            <td style={{ border: '1px solid #ddd', padding: '8px' }}>{tx.id}</td>
                            <td style={{ border: '1px solid #ddd', padding: '8px' }}>{tx.date}</td>
                            <td style={{ border: '1px solid #ddd', padding: '8px' }}>{tx.type}</td>
                            <td style={{ border: '1px solid #ddd', padding: '8px' }}>{tx.category}</td>
                            <td style={{ border: '1px solid #ddd', padding: '8px' }}>{tx.amount}</td>
                            <td style={{ border: '1px solid #ddd', padding: '8px' }}>{tx.memo}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    )
}

export default TransactionList;