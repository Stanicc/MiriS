import styles from '../styles/index.module.css';
import Head from 'next/head';
import Script from 'next/script';
import React, { useEffect, useState } from "react";

const features = getFeatures()
let currentFeature = 0

export default function Home() {
  /*

  Header navigation

  */
  const [headerClass, setHeaderClass] = useState(`${styles.header} fixed-top`)
  const [mobileNavigationButtonClass, setMobileNavigationButtonClass] = useState(`${styles.mobile_navigation_toggle} d-lg-none`)
  
  function changeLayout(scrolled: boolean) {
    if (scrolled == true) setHeaderClass(`${styles.header_scrolled} fixed-top`)
    else setHeaderClass("fixed-top")
  }

  const handleScroll = () => {
    if (window.scrollY > 50) changeLayout(true)
    else changeLayout(false)
  }

  const handleWindowSize = () => {
    if (window.innerHeight < 750) {
      setMobileNavigationButtonClass(`${styles.mobile_navigation_toggle} d-lg-none`)
    } else {
      setMobileNavigationButtonClass(`${{display: "none"}}`)
    }
  }

  /*

  Features pagination

  */

  const [feature, setFeature] = useState(features[0])
  const [paginationClass, setPaginationClass] = useState(`${styles.pagination}`)

  function handleFeatureClick(next: boolean) {
    if (next) {
      if (currentFeature < (features.length - 1)) {
        currentFeature += 1
        setPaginationClass(`${styles.pagination} ${styles.transition_next}`)
      } else {
        currentFeature = -1
        handleFeatureClick(false)
        }
    } else {
      if (currentFeature == 0) {
        currentFeature = features.length - 2
        handleFeatureClick(true)
        return
      }
      if (currentFeature == -1) currentFeature = 1

      currentFeature -= 1
      setPaginationClass(`${styles.pagination} ${styles.transition_previous}`)
    }

    setTimeout(() => {
      setPaginationClass(`${styles.pagination}`)
    }, 500)
    setFeature(features[currentFeature])
  }

  /*
    Use effect
  */
  useEffect(() => {
    window.addEventListener('scroll', handleScroll)
    window.addEventListener('resize', handleWindowSize)

    handleWindowSize
    return () => {
      window.removeEventListener('scroll', handleScroll)
      window.removeEventListener('resize', handleWindowSize)
    }
  })
  return (
    <>
    <header className={headerClass.toString()}>
      <div className="container">
        <nav className={`${styles.navigation} d-none d-lg-block`}>
          <ul>
            <li><a href="#about" onClick={() => {
            const element = document.getElementById(styles.about);
            if (element) {
              element.scrollIntoView({
                behavior: 'smooth',
                block: 'start',
                inline: 'nearest',
              });
            }
            }}>About</a></li>
            <li className={styles.home}><a href="#" onClick={() => {
            const element = document.getElementById(styles.home_area);
            if (element) {
              element.scrollIntoView({
                behavior: 'smooth',
                block: 'start',
                inline: 'nearest',
              });
            }
            }}>Home</a></li>
            <li><a href="dashboard">Dashboard</a></li>
          </ul>
       </nav>
      </div>
    </header>

    <nav className={`${styles.mobile_navigation} d-lg-none`}>
    </nav>
    <button type="button" className={mobileNavigationButtonClass}>
      <i className="fa fa-bars" />
    </button>

    <section id={styles.home_area}>
      <section id={styles.home}>
        <div className="container">
          <br />
          <br />
          <br />
          <br />
          <br />
          <br />
          <br />

          <img src="miris2.png" alt="." className={styles.logo_home} />
          <div className={styles.text}><h2>MiriS</h2></div>
          <div className={styles.text}><p>Turning your server into a party</p></div>

          <div className={styles.home_invite_button_center}>
            <a target="_blank" href="https://discord.gg/zFKJnCtgjW" className={styles.home_invite_button}>Add me</a>
          </div>
        </div>
      </section>
    </section>

    <section className={`${styles.overview_area} ${styles.section_padding}`}>
      <div className="container">
      <div className="row">
          <div className="col-md-12">
            <div className={styles.section_title}>

              <div className="music_body">
                <div className="music_note">
                  <div className="note_1">
                    &#9835; &#9833;
                  </div>
                  <div className="note_1">
                    &#9835; &#9833;
                  </div>
                  <div className="note_2">
                    &#9833;
                  </div>
                  <div className="note_3">
                    &#9839; &#9834;
                  </div>
                  <div className="note_4">
                    &#9834;
                  </div>
                </div>  
              </div> 

              <h1>Aiming at quality</h1>
              <p>Have fun with my functions</p>
            </div>
          </div>
        </div>

        <div className={styles.features}>
        <div className={styles.title}>
              <h3>{feature.name}</h3>
            </div>
            <div className={styles.description}>
              <p className={feature.textPosition}>{feature.description}</p>
              <img className={feature.iconPosition} src={feature.icon} alt="Feature Image" />
            </div>
          <div className={paginationClass}>
            <div className={styles.pagination_container}>
            <svg onClick={() => handleFeatureClick(false)} className={`${styles.arrow} ${styles.arrow_previous}`} height="96" viewBox="0 0 24 24" width="96" xmlns="http://www.w3.org/2000/svg">
              <path d="M15.41 16.09l-4.58-4.59 4.58-4.59L14 5.5l-6 6 6 6z"/>
              <path d="M0-.5h24v24H0z" fill="none"/>
            </svg>

              <div className={`${styles.dot} ${styles.dot_first}`} />
              <div className={styles.dot}>
                <div className={styles.big_dot_container}>
                  <div className={styles.big_dot}></div>
                </div>
              </div>
              <div className={`${styles.dot} ${styles.dot_last}`} />

              <svg onClick={() => handleFeatureClick(true)} className={`${styles.arrow} ${styles.arrow_next}`} height="96" viewBox="0 0 24 24" width="96" xmlns="http://www.w3.org/2000/svg">
              <path d="M8.59 16.34l4.58-4.59-4.58-4.59L10 5.75l6 6-6 6z"/>
              <path d="M0-.25h24v24H0z" fill="none"/>
            </svg>
            </div>

          </div>
        </div>

      </div>
    </section>

    <section id={styles.about}>
      <div className="container">
        <div className="row justify-content-center text-center mb-5">
          <div className="col-md-6 mb-5" />
        </div>

        <div className="music_body">
          <div className="music_note">
            <div className="note_1">
              &#9835; &#9833;
            </div>
            <div className="note_1">
              &#9835; &#9833;
            </div>
            <div className="note_2">
              &#9833;
            </div>
            <div className="note_3">
              &#9839; &#9834;
            </div>
            <div className="note_4">
              &#9834;
            </div>
          </div>  
        </div>

        <h2>About</h2>

        <div className={styles.about_body}>
          <p>Here is some information about me</p>
        </div>

        <div className="row">
          <div className="col-md-10">
            <div className={styles.card}>
              <h3>Just a simple Discord bot</h3>

              <div className={styles.card_text}>
                <p><strong>MiriS</strong> Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam ut risus lacus. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Suspendisse velit magna, accumsan non lacus nec, rhoncus viverra lectus. Sed quis ante turpis. Quisque massa mi, convallis non arcu sed, scelerisque efficitur massa. Duis tincidunt lacus at est imperdiet pharetra. Curabitur sapien nibh, tincidunt venenatis velit id, pulvinar tincidunt lorem. Nam malesuada massa a diam venenatis, in hendrerit nunc consequat. Praesent ut est non leo vulputate tristique. Vestibulum venenatis pretium rhoncus. Aenean id congue felis. Suspendisse et blandit felis.</p>
              </div>

              <div className="col-md-15">
                <a target="_blank" href="discord"><button className={styles.about_discord_button}>Discord server</button></a>
              </div>
            </div>
          </div>

          <div className={`col-md-1 ${styles.discordserver}`}>
            <iframe src="https://discord.com/widget?id=854761435086848060&theme=dark" width="350" height="500" allowTransparency={true} frameBorder="0"></iframe>
          </div>
        </div>

      </div>
    </section>

    <section id={styles.utilities_card}>
      <div className="container">
      <div className="music_body">
          <div className="music_note">
            <div className="note_1">
              &#9835; &#9833;
            </div>
            <div className="note_1">
              &#9835; &#9833;
            </div>
            <div className="note_2">
              &#9833;
            </div>
            <div className="note_3">
              &#9839; &#9834;
            </div>
            <div className="note_4">
              &#9834;
            </div>
          </div>  
        </div>

        <div className="row">

          <div className="col-md-4">
            <div className={`${styles.uc} wow bounceInUp`}>
              <div className={styles.centered}>
                <i className="fa fa-question" />
              </div>

              <h3>Confused?</h3>
              <div className={styles.card_text}>
                <p>Are you confused about any of my systems? I have a page with all the information about my functions!</p>
              </div>
              <div className="text-center">
                <a href="documentation"><button type="button" className={styles.button}>See documentation</button></a>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className={`${styles.uc} wow bounceInUp`}>
              <div className={styles.centered}>
                <i className="fa fa-phone" />
              </div>

              <h3>Need help?</h3>
              <div className={styles.card_text}>
                <p>Looking for support? Join in my Discord server and open a help ticket!</p>
              </div>
              <div className="text-center">
                <a href="discord"><button type="button" className={styles.button}>Join Discord server</button></a>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className={`${styles.uc} wow bounceInUp`}>
              <div className={styles.centered}>
                <i className="fa fa-star" />
              </div>

              <h3>Premium</h3>
              <div className={styles.card_text}>
                <p>How about having access to premium functions? I've several commands to your enjoyment</p>
              </div>
              <div className="text-center">
                <a href="premium"><button type="button" className={styles.button}>See plans</button></a>
              </div>
            </div>
          </div>

        </div>
      </div>
    </section>

    <footer id={styles.footer}>
      <div className="footer-top">
        <div className="container">
          <div className="row">
            <div className="container">
              <div className={styles.copyright}>
                © 2021 - MiriS <br />
                <a href="discord">
                  <span className={styles.footer_title}>Just a simple Discord bot</span>
                  <span><br />&nbsp;</span>
                  </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </footer>

    <div id="loading">
      <div className="background_loading">
        <div id="preloader">
          <span></span>
          <span></span>
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
    </div>

    <script src="/scripts/jquery/jquery.min.js" />
    <script src="/scripts/jquery/jquery-migrate.min.js" />
    <script src="/scripts/wow/wow.min.js" />
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.min.js"
      integrity="sha384-ChfqqxuZUCnJSK3+MXmPNIyE6ZbWh2IMqE241rYiqJxyMiZ6OW/JmZQ5stwEULTy"
      crossOrigin="anonymous" />
    <Script strategy="beforeInteractive" src="/scripts/index.js"/>
    </>
  )
}

function getFeatures() {
  const features = [
    {
    icon: "msplay.gif",
    name: "Music",
    description: "Play music from different platforms, create your own playlist with your favorite songs, add effects to the player and more!",
    textPosition: styles.in_right,
    iconPosition: styles.in_left_image
    },
    {
      icon: "mssearch.gif",
      name: "Search",
      description: "Download songs, search for artists, albums, songs and lyrics, convert video to songs, search for a song by lyrics and more!",
      textPosition: styles.in_left,
      iconPosition: styles.in_right_image
    },
    {
      icon: "config.png",
      name: "Dashboard",
      description: "...",
      textPosition: styles.in_right,
      iconPosition: styles.in_left_image
    }
  ]

  return features
}