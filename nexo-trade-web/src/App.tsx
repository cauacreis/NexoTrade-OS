import { useEffect, useState } from 'react';
import axios from 'axios';

interface Wallet {
  id: number;
  usdBalance: number;
  btcBalance: number;
}

const API_BASE = 'http://localhost:8080/api';

function App() {
  const [wallet, setWallet] = useState<Wallet | null>(null);
  const [botActive, setBotActive] = useState(false);
  const [buyAmount, setBuyAmount] = useState('1000');
  const [sellAmount, setSellAmount] = useState('0.1');
  const [error, setError] = useState('');

  const fetchWallet = async () => {
    try {
      const res = await axios.get(`${API_BASE}/wallet`);
      setWallet(res.data);
      setError('');
    } catch (err: any) {
      console.error(err);
      setError('Erro ao carregar carteira. Backend está rodando?');
    }
  };

  useEffect(() => {
    fetchWallet();
    const interval = setInterval(fetchWallet, 2000);
    return () => clearInterval(interval);
  }, []);

  const toggleBot = async () => {
    try {
      const res = await axios.post(`${API_BASE}/bot/toggle`);
      setBotActive(res.data.active);
      setError('');
    } catch (err: any) {
      setError('Erro ao alternar o bot.');
    }
  };

  const executeTrade = async (action: 'BUY' | 'SELL') => {
    try {
      const payload = action === 'BUY' 
        ? { action, amountUsd: parseFloat(buyAmount) } 
        : { action, amountBtc: parseFloat(sellAmount) };
        
      await axios.post(`${API_BASE}/trade/execute`, payload);
      fetchWallet(); // Atualiza logo após operar
      setError('');
    } catch (err: any) {
      setError(err.response?.data || 'Erro na operação de trade.');
    }
  };

  return (
    <div className="min-h-screen p-8 flex flex-col items-center">
      <h1 className="text-5xl font-black mb-12 tracking-tighter uppercase text-zinc-100 shadow-[4px_4px_0px_0px_#22c55e] border-4 border-zinc-100 p-4 bg-zinc-900">
        NexoTrade OS Terminal
      </h1>

      {error && (
        <div className="bg-red-500 text-zinc-950 font-bold p-4 border-4 border-zinc-100 shadow-[8px_8px_0px_0px_rgba(255,255,255,1)] mb-8 w-full max-w-2xl">
          ERROR: {error}
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8 w-full max-w-5xl">
        {/* Cofre */}
        <div className="border-4 border-zinc-100 p-8 bg-zinc-900 shadow-[8px_8px_0px_0px_rgba(255,255,255,1)] flex flex-col justify-between">
          <h2 className="text-2xl font-bold border-b-4 border-zinc-100 pb-4 mb-6 uppercase">O Cofre</h2>
          <div className="flex flex-col gap-4">
            <div className="bg-zinc-800 p-4 border-2 border-zinc-500">
              <span className="text-zinc-400 text-sm block mb-1">SALDO USD</span>
              <span className="text-4xl font-black text-green-400">
                $ {wallet ? wallet.usdBalance.toLocaleString('en-US', { minimumFractionDigits: 2 }) : '0.00'}
              </span>
            </div>
            <div className="bg-zinc-800 p-4 border-2 border-zinc-500">
              <span className="text-zinc-400 text-sm block mb-1">SALDO BTC</span>
              <span className="text-4xl font-black text-orange-400">
                ₿ {wallet ? wallet.btcBalance.toFixed(8) : '0.00000000'}
              </span>
            </div>
          </div>
        </div>

        <div className="flex flex-col gap-8">
          {/* Cérebro (Bot Toggle) */}
          <div className="border-4 border-zinc-100 p-8 bg-zinc-900 shadow-[8px_8px_0px_0px_rgba(255,255,255,1)]">
            <h2 className="text-2xl font-bold border-b-4 border-zinc-100 pb-4 mb-6 uppercase">O Cérebro</h2>
            <button
              onClick={toggleBot}
              className={`w-full py-6 text-3xl font-black border-4 border-zinc-100 transition-all uppercase 
                ${botActive 
                  ? 'bg-lime-400 text-zinc-950 shadow-[8px_8px_0px_0px_#a3e635] translate-x-[-4px] translate-y-[-4px]' 
                  : 'bg-zinc-800 text-zinc-400 shadow-[8px_8px_0px_0px_rgba(255,255,255,1)] hover:bg-zinc-700'
                }`}
            >
              {botActive ? 'BOT ATIVADO (SMA)' : 'BOT DESLIGADO'}
            </button>
          </div>

          {/* Override Manual */}
          <div className="border-4 border-zinc-100 p-8 bg-zinc-900 shadow-[8px_8px_0px_0px_rgba(255,255,255,1)]">
            <h2 className="text-2xl font-bold border-b-4 border-zinc-100 pb-4 mb-6 uppercase">Override Manual</h2>
            
            <div className="flex flex-col gap-6">
              <div className="flex gap-4 items-center">
                <input 
                  type="number" 
                  value={buyAmount}
                  onChange={(e) => setBuyAmount(e.target.value)}
                  className="bg-zinc-800 text-xl font-bold p-4 border-4 border-zinc-100 w-1/3 outline-none focus:border-green-400"
                  placeholder="USD"
                />
                <button 
                  onClick={() => executeTrade('BUY')}
                  className="flex-1 bg-green-500 hover:bg-green-400 text-zinc-950 text-2xl font-black p-4 border-4 border-zinc-100 shadow-[6px_6px_0px_0px_#22c55e] active:translate-x-1 active:translate-y-1 active:shadow-none transition-all"
                >
                  COMPRAR BTC
                </button>
              </div>

              <div className="flex gap-4 items-center">
                <input 
                  type="number" 
                  value={sellAmount}
                  onChange={(e) => setSellAmount(e.target.value)}
                  className="bg-zinc-800 text-xl font-bold p-4 border-4 border-zinc-100 w-1/3 outline-none focus:border-red-400"
                  placeholder="BTC"
                  step="0.01"
                />
                <button 
                  onClick={() => executeTrade('SELL')}
                  className="flex-1 bg-red-500 hover:bg-red-400 text-zinc-950 text-2xl font-black p-4 border-4 border-zinc-100 shadow-[6px_6px_0px_0px_#ef4444] active:translate-x-1 active:translate-y-1 active:shadow-none transition-all"
                >
                  VENDER BTC
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
