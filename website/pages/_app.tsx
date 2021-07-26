import type { AppProps } from 'next/app'
import Head from 'next/head'
import '../styles/lib/bootstrap/css/bootstrap.min.css'
import '../styles/lib/font-awesome/css/font-awesome.min.css'
import '../styles/lib/wow/wow.css'
import '../styles/global.css'

function App({ Component, pageProps }: AppProps) {
  return (
    <>
      <Head>
        <title>MiriS - A discord music bot!</title>
        <meta name="charset" content="wtf-8" />
        <meta name="description" content="MiriS - A discord music bot!" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <link rel="icon" href="headphone.png" />
        <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Open+Sans:300,300i,400,400i,700,700i|Montserrat:300,400,500,700"/>
      </Head>
      <Component {...pageProps} />
    </>
  )
}
export default App