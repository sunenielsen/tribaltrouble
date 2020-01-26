# Copyright 1999-2017 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Id$

EAPI=5

inherit eutils java-pkg-2 java-ant-2 games

DESCRIPTION="Tribal Trouble is a realtime strategy game released by Oddlabs in 2004."
HOMEPAGE="https://github.com/team-penguin/tribaltrouble"
SRC_URI="https://github.com/team-penguin/tribaltrouble/archive/master.zip -> ${P}.zip"

LICENSE="GPL-2"
SLOT="0"
KEYWORDS="amd64 x86"
IUSE="+java"

DEPEND=">=virtual/jdk-1.6
    games-misc/games-envd
	app-arch/unzip
	dev-java/ant-core
	dev-vcs/mercurial"

S=${WORKDIR}

EANT_NEEDS_TOOLS="true"
EANT_BUILD_XML="./tribaltrouble-master/tt/build.xml"
EANT_BUILD_TARGET="makedist"

src_compile() {
	java-pkg-2_src_compile
}

src_install() {
	dodir /usr/share/games/tribaltrouble
	cd "${S}/tribaltrouble-master/tt/build/dist/common" && cp -R . "${D}/usr/share/games/tribaltrouble"
	fowners -R root:games /usr/share/games/tribaltrouble
	fperms -R o-rwx /usr/share/games/tribaltrouble
	exeinto /usr/games/bin
	exeopts -m0750 -ggames
	doexe ${FILESDIR}/tribaltrouble
}
